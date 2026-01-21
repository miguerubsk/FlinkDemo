package org.myorg.quickstart.orchestrator;

import org.myorg.quickstart.config.AppConfig;
import org.myorg.quickstart.infra.FlinkRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job Orchestrator (Clase Matriz)
 * <p>
 * Responsable de arrancar el Trabajo de Ingestión de CDC y disparar trabajos de
 * enriquecimiento
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
                String jobClass = AppConfig.get("jobs.api-enrichment", "org.myorg.quickstart.jobs.ApiEnrichmentJob");
                LOG.info(">>> [MATRIZ] Disparando ENRIQUECIMIENTO para un evento...");
                new FlinkRestClient().submitJob(jobClass, data);
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
                String jobClass = AppConfig.get("jobs.mongo-sink", "org.myorg.quickstart.jobs.MongoSinkJob");
                LOG.info(">>> [MATRIZ] Disparando PERSISTENCIA en MongoDB...");
                new FlinkRestClient().submitJob(jobClass, data);
            } catch (Exception e) {
                LOG.error(">>> [MATRIZ] Error al disparar el trabajo de persistencia", e);
            }
        }).start();
    }
}