package org.myorg.quickstart.orchestrator;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties properties = new Properties();
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    static {
        // Configuramos la URL del JobManager
        String defaultJm = System.getProperty("os.name").toLowerCase().contains("win") ? "localhost"
                : "flink_jobmanager";

        String jmUrl = getEnv("FLINK_JOBMANAGER_URL", "http://" + defaultJm + ":8081");
        properties.setProperty("flink.jobmanager.url", jmUrl);
        properties.setProperty("flink.jars.endpoint", "/jars");

        // Mapeo de nombres de jobs a sus clases implementadoras
        properties.setProperty("jobs.cdc-ingestion", "org.myorg.quickstart.jobs.CdcIngestionJob");
        properties.setProperty("jobs.api-enrichment", "org.myorg.quickstart.jobs.ApiEnrichmentJob");
        properties.setProperty("jobs.normalization-validation", "org.myorg.quickstart.jobs.NormalizationValidationJob");
        properties.setProperty("jobs.mongo-sink", "org.myorg.quickstart.jobs.MongoSinkJob");
    }

    /**
     * Obtiene una variable de entorno de forma resiliente:
     * 1. Busca en el objeto Dotenv (fichero .env)
     * 2. Busca en las variables de entorno del sistema (System.getenv)
     * 3. Devuelve el valor por defecto
     */
    public static String getEnv(String key, String defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        if (value == null || value.isEmpty()) {
            value = System.getProperty(key);
        }
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getFlinkJobManagerUrl() {
        return properties.getProperty("flink.jobmanager.url");
    }

    public static String getFlinkJarsEndpoint() {
        return properties.getProperty("flink.jars.endpoint");
    }
}
