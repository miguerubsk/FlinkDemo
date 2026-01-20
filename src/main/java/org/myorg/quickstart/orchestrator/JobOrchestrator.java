package org.myorg.quickstart.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Job Orchestrator (Clase Matriz)
 * <p>
 * Responsable de arrancar el Job de CDC y disparar trabajos de enriquecimiento
 * bajo demanda.
 */
public class JobOrchestrator {
    private static final Logger LOG = LoggerFactory.getLogger(JobOrchestrator.class);

    public static void main(String[] args) {
        LOG.info("==========================================================");
        LOG.info(">>> MATRIZ DE PIPELINE: Arrancando sistema de orquestación");
        LOG.info("==========================================================");
        try {
            // En lugar de usar la API REST para el primer trabajo, llamamos directamente a
            // su main.
            // Esto permite que use createRemoteEnvironment con el JAR local de forma
            // transparente.
            LOG.info(">>> [MATRIZ] Lanzando trabajo de escucha continua (CdcIngestionJob)...");
            org.myorg.quickstart.jobs.CdcIngestionJob.main(args);
        } catch (Exception e) {
            LOG.error(">>> [MATRIZ] ERROR CRÍTICO: No se pudo arrancar el sistema", e);
        }
    }

    /**
     * Dispara el trabajo de enriquecimiento para un registro específico.
     * Este método se llama desde el SINK de un trabajo que ya está corriendo en
     * Flink.
     */
    public static void triggerEnrichment(String data) {
        new Thread(() -> {
            try {
                String jobClass = ConfigLoader.getOrDefault("jobs.api-enrichment", "org.myorg.quickstart.jobs.ApiEnrichmentJob");
                LOG.info(">>> [MATRIZ] Disparando ENRIQUECIMIENTO para un evento...");
                submitJob(jobClass, data);
            } catch (Exception e) {
                LOG.error(">>> [MATRIZ] Error al disparar el trabajo de enriquecimiento", e);
            }
        }).start();
    }

    /**
     * Dispara el trabajo de persistencia en MongoDB.
     */
    public static void triggerMongoSink(String data) {
        new Thread(() -> {
            try {
                String jobClass = ConfigLoader.getOrDefault("jobs.mongo-sink", "org.myorg.quickstart.jobs.MongoSinkJob");
                LOG.info(">>> [MATRIZ] Disparando PERSISTENCIA en MongoDB...");
                submitJob(jobClass, data);
            } catch (Exception e) {
                LOG.error(">>> [MATRIZ] Error al disparar el trabajo de persistencia", e);
            }
        }).start();
    }

    private static String getLatestJarId() throws Exception {
        String url = ConfigLoader.getFlinkJobManagerUrl() + ConfigLoader.getFlinkJarsEndpoint();
        LOG.info("Consultando JARs en: {}", url);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            JsonNode jsonResponse = new ObjectMapper().readTree(response.toString());
            if (jsonResponse.has("files") && jsonResponse.get("files").size() > 0) {
                return jsonResponse.get("files").get(0).get("id").asText();
            }
        } finally {
            conn.disconnect();
        }
        throw new Exception("No se encontraron JARs en Flink");
    }

    private static String submitJob(String entryClass, String arg) throws Exception {
        String jarId = getLatestJarId();
        String url = ConfigLoader.getFlinkJobManagerUrl() + "/jars/" + jarId + "/run";

        ObjectMapper mapper = new ObjectMapper();
        var payload = mapper.createObjectNode();
        payload.put("entryClass", entryClass);
        if (arg != null) {
            // Usamos programArgsList para evitar problemas de escape de strings
            payload.putArray("programArgsList").add(arg);
        }

        String jsonPayload = mapper.writeValueAsString(payload);
        LOG.info("Enviando POST a {} con payload: {}", url, jsonPayload);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes("UTF-8"));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = in.readLine();
                JsonNode jsonResponse = mapper.readTree(response);
                String jobId = jsonResponse.get("jobid").asText();
                LOG.info("Trabajo {} enviado con éxito. jobId={}", entryClass, jobId);
                return jobId;
            }
        } else {
            // Leer el cuerpo del error para depurar mejor
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                String line;
                while ((line = err.readLine()) != null) {
                    errorResponse.append(line);
                }
            }
            LOG.error("Error al enviar trabajo {}. Código: {}. Mensaje: {}", entryClass, responseCode, errorResponse.toString());
        }
        conn.disconnect();
        return null;
    }
}