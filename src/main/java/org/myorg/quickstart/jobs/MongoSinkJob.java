package org.myorg.quickstart.jobs;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.myorg.quickstart.tasks.MongoSinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSinkJob {
    private static final Logger LOG = LoggerFactory.getLogger(MongoSinkJob.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LOG.error(">>> [SINK MONGO] Error: No se recibieron datos para persistir.");
            return;
        }

        String inputJson = args[0];
        LOG.info(">>> [SINK MONGO] Iniciando persistencia en lote...");

        // Parsear la entrada como una lista (Batch)
        java.util.List<String> batch;
        try {
            batch = MAPPER.readValue(inputJson,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {
                    });
            LOG.info(">>> [SINK MONGO] Persistiendo lote de {} elementos.", batch.size());
        } catch (Exception e) {
            LOG.warn(">>> [SINK MONGO] Entrada no es un lote JSON. Procesando como elemento único.");
            batch = java.util.List.of(inputJson);
        }

        // Usamos el entorno de ejecución actual del cluster
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(org.apache.flink.api.common.RuntimeExecutionMode.BATCH);
        env.setParallelism(1);

        // 1. Fuente: Todos los elementos del lote
        DataStream<String> sourceStream = env.fromCollection(batch);

        // 2. Sink: Publicar en MongoDB
        LOG.info(">>> [SINK MONGO] Enviando datos a la colección historico_siniestros...");
        MongoSinkTask.sink(sourceStream);

        env.execute("MongoSinkJob (Batch-Mode)");
        LOG.info(">>> [SINK MONGO] Proceso de persistencia finalizado.");
    }
}
