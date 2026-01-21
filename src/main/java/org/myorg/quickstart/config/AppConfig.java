package org.myorg.quickstart.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;

public class AppConfig {

    private static AppConfig instance;
    private final Properties properties;
    private final Dotenv dotenv;

    private AppConfig() {
        properties = new Properties();
        dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Default Configuration
        String defaultJm = System.getProperty("os.name").toLowerCase().contains("win") ? "localhost"
                : "flink_jobmanager";
        String jmUrl = getEnv("FLINK_JOBMANAGER_URL", "http://" + defaultJm + ":8081");

        properties.setProperty("flink.jobmanager.url", jmUrl);
        properties.setProperty("flink.jars.endpoint", "/jars");

        // Job Mappings
        properties.setProperty("jobs.cdc-ingestion", "org.myorg.quickstart.jobs.CdcIngestionJob");
        properties.setProperty("jobs.api-enrichment", "org.myorg.quickstart.jobs.ApiEnrichmentJob");
        properties.setProperty("jobs.normalization-validation", "org.myorg.quickstart.jobs.NormalizationValidationJob");
        properties.setProperty("jobs.mongo-sink", "org.myorg.quickstart.jobs.MongoSinkJob");
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getEnv(String key, String defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    // Static helper for backward compatibility/ease of use
    public static String get(String key, String defaultValue) {
        return getInstance().getEnv(key, defaultValue);
    }

    public static String getProp(String key, String defaultValue) {
        return getInstance().getProperty(key, defaultValue);
    }
}
