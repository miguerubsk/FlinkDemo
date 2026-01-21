package org.myorg.quickstart.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.myorg.quickstart.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlinkRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(FlinkRestClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public String submitJob(String entryClass, String... args) throws Exception {
        String jarId = getLatestJarId();
        String jmUrl = AppConfig.getProp("flink.jobmanager.url", "http://localhost:8081");
        String url = jmUrl + "/jars/" + jarId + "/run";

        var payload = mapper.createObjectNode();
        payload.put("entryClass", entryClass);
        if (args != null && args.length > 0) {
            var argsArray = payload.putArray("programArgsList");
            for (String arg : args) {
                argsArray.add(arg);
            }
        }

        String jsonPayload = mapper.writeValueAsString(payload);
        LOG.info("Submitting job {} to {}", entryClass, url);

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
                JsonNode jsonResponse = mapper.readTree(in.readLine());
                String jobId = jsonResponse.get("jobid").asText();
                LOG.info("Job submitted successfully. JobID: {}", jobId);
                return jobId;
            }
        } else {
            handleError(conn);
            return null;
        }
    }

    private String getLatestJarId() throws Exception {
        String jmUrl = AppConfig.getProp("flink.jobmanager.url", "http://localhost:8081");
        String endpoint = AppConfig.getProp("flink.jars.endpoint", "/jars");
        String url = jmUrl + endpoint;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            JsonNode jsonResponse = mapper.readTree(in.readLine());
            if (jsonResponse.has("files") && jsonResponse.get("files").size() > 0) {
                return jsonResponse.get("files").get(0).get("id").asText();
            }
        }
        throw new RuntimeException("No JARs found in Flink Cluster at " + url);
    }

    private void handleError(HttpURLConnection conn) throws Exception {
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) {
                errorResponse.append(line);
            }
        }
        LOG.error("Flink API Error ({}}: {}", conn.getResponseCode(), errorResponse.toString());
        throw new RuntimeException("Failed to submit job: " + errorResponse.toString());
    }
}
