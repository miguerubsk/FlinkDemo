package org.myorg.quickstart.infra;

import org.myorg.quickstart.config.AppConfig;
import org.myorg.quickstart.domain.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSiniestroRepository implements SiniestroRepository {

    private Connection connection;

    @Override
    public void open() throws Exception {
        Class.forName("org.postgresql.Driver");
        AppConfig config = AppConfig.getInstance();

        String host = config.getEnv("POSTGRES_HOST", "postgres_cdc");
        String port = config.getEnv("POSTGRES_PORT", "5432");
        String db = config.getEnv("POSTGRES_DB", "mi_base_datos");
        String user = config.getEnv("POSTGRES_USER", "usuario_flink");
        String pass = config.getEnv("POSTGRES_PASSWORD", "password_flink");

        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        this.connection = DriverManager.getConnection(url, user, pass);
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public Optional<Siniestro> findSiniestroById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM siniestros WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Siniestro s = new Siniestro();
                    s.setId(rs.getInt("id"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setEstado(rs.getString("estado"));
                    s.setUltimaActualizacion(rs.getTimestamp("ultima_actualizacion").toInstant().toString());
                    s.setClienteId(rs.getInt("cliente_id"));
                    s.setExpedienteId(rs.getInt("expediente_id"));
                    return Optional.of(s);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Siniestro", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Cliente> findClienteById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("dni_cif"),
                            rs.getString("tipo_cliente")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Cliente", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Expediente> findExpedienteById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM expedientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Expediente(
                            rs.getInt("id"),
                            rs.getString("codigo_expediente"),
                            rs.getDate("fecha_apertura").toString(),
                            rs.getString("sucursal")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Expediente", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Servicio> findServiciosBySiniestroId(int id) {
        List<Servicio> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM servicios WHERE siniestro_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Servicio(
                            rs.getString("proveedor"),
                            rs.getString("tipo_servicio"),
                            rs.getDouble("coste")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Servicios", e);
        }
        return list;
    }
}
