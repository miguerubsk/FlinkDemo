package org.myorg.quickstart.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.myorg.quickstart.orchestrator.ConfigLoader;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnrichmentMapFunction extends RichMapFunction<String, String> {

    private transient Connection dbConn;
    private transient ObjectMapper mapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        // Establecemos conexión directa para los lookups
        Class.forName("org.postgresql.Driver");

        String host = ConfigLoader.getEnv("POSTGRES_HOST", "postgres_cdc");
        String port = ConfigLoader.getEnv("POSTGRES_PORT", "5432");
        String db = ConfigLoader.getEnv("POSTGRES_DB", "mi_base_datos");
        String user = ConfigLoader.getEnv("POSTGRES_USER", "usuario_flink");
        String pass = ConfigLoader.getEnv("POSTGRES_PASSWORD", "password_flink");

        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);

        dbConn = DriverManager.getConnection(url, user, pass);
        mapper = new ObjectMapper();
    }

    @Override
    public void close() throws Exception {
        if (dbConn != null) {
            dbConn.close();
        }
    }

    @Override
    public String map(String value) throws Exception {
        JsonNode root = mapper.readTree(value);
        System.out.println("DEBUG CDC EVENT: " + root.toString());
        JsonNode data = root.get("after");
        if (data == null) {
            return null; // Ignorar borrados
        }

        JsonNode source = root.get("source");
        String sourceTable = source.get("table").asText();

        // Solo nos interesa si el evento viene de 'siniestros' o 'servicios'
        // (En un caso real, podrías querer reaccionar a clientes explícitamente,
        // pero aquí asumimos que el driver es 'siniestros' o 'servicios' para
        // recomponer todo el agregado).

        int siniestroId = -1;

        switch (sourceTable) {
            case "siniestros":
                siniestroId = data.get("id").asInt();
                break;
            case "servicios":
                siniestroId = data.get("siniestro_id").asInt();
                break;
            default:
                System.out.println("DEBUG IGNORING TABLE: " + sourceTable);
                return null;
        }

        return buildCompositeJson(siniestroId);
    }

    private String buildCompositeJson(int id) throws Exception {
        // Reconstruimos TODA la info del siniestro haciendo lookups síncronos a
        // Postgres
        // (Esto no es lo más eficiente en alta carga, pero es simple para el ejemplo).

        // 1. Datos del Siniestro
        PreparedStatement ps = dbConn.prepareStatement("SELECT * FROM siniestros WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            ObjectNode doc = mapper.createObjectNode();
            doc.put("siniestro_id", id);
            // doc.put("fecha_siniestro", rs.getDate("fecha_siniestro").toString()); //
            // Columna no existe
            doc.put("ultima_actualizacion", rs.getTimestamp("ultima_actualizacion").toInstant().toString());
            doc.put("descripcion", rs.getString("descripcion"));
            doc.put("estado", rs.getString("estado"));

            // FKs
            int clienteId = rs.getInt("cliente_id");
            int expedienteId = rs.getInt("expediente_id");

            // 2. Consulta de Cliente
            PreparedStatement psCl = dbConn.prepareStatement("SELECT * FROM clientes WHERE id = ?");
            psCl.setInt(1, clienteId);
            ResultSet rsCl = psCl.executeQuery();
            if (rsCl.next()) {
                ObjectNode clienteNode = doc.putObject("cliente");
                clienteNode.put("id", clienteId);
                clienteNode.put("nombre", rsCl.getString("nombre"));
                clienteNode.put("dni_cif", rsCl.getString("dni_cif"));
                clienteNode.put("tipo", rsCl.getString("tipo_cliente"));
            }

            // 3. Consulta de Expediente
            PreparedStatement psExp = dbConn.prepareStatement("SELECT * FROM expedientes WHERE id = ?");
            psExp.setInt(1, expedienteId);
            ResultSet rsExp = psExp.executeQuery();
            if (rsExp.next()) {
                ObjectNode expNode = mapper.createObjectNode();
                expNode.put("codigo", rsExp.getString("codigo_expediente"));
                expNode.put("apertura", rsExp.getDate("fecha_apertura").toString());
                expNode.put("sucursal", rsExp.getString("sucursal"));
                // Lo añadimos como array para compatibilidad con formato anterior si es
                // necesario
                doc.putArray("expedientes").add(expNode);
            }

            // 4. Consulta de Servicios (Array)
            List<Map<String, Object>> servicios = new ArrayList<>();
            PreparedStatement psServ = dbConn.prepareStatement("SELECT * FROM servicios WHERE siniestro_id = ?");
            psServ.setInt(1, id);
            ResultSet rsServ = psServ.executeQuery();
            while (rsServ.next()) {
                servicios.add(Map.of(
                        "proveedor", rsServ.getString("proveedor"),
                        "tipo", rsServ.getString("tipo_servicio"),
                        "coste", rsServ.getDouble("coste")));
            }
            doc.putPOJO("servicios", servicios);

            try {
                return mapper.writeValueAsString(doc);
            } catch (JsonProcessingException ex) {
                System.getLogger(EnrichmentMapFunction.class.getName()).log(System.Logger.Level.ERROR, (String) null,
                        ex);
                return null;
            }
        }
        System.out.println("DEBUG ENRICHMENT FAILED for ID: " + id);
        return null;
    }
}
