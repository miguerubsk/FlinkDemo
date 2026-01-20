package org.myorg.quickstart.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.myorg.quickstart.functions.EnrichmentMapFunction;
import org.myorg.quickstart.orchestrator.JobOrchestrator;

import java.util.ArrayList;
import java.util.List;

public class ApiEnrichmentJob {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApiEnrichmentJob.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            LOG.error(">>> [ENRIQUECIMIENTO] Error: No se recibieron datos de entrada.");
            return;
        }

        String inputJson = args[0];
        LOG.info(">>> [ENRIQUECIMIENTO] Iniciando proceso para un lote de eventos...");

        // Parsear la entrada como una lista (Batch)
        List<String> batchInputs;
        try {
            batchInputs = MAPPER.readValue(inputJson, new TypeReference<List<String>>() {
            });
            LOG.info(">>> [ENRIQUECIMIENTO] Procesando lote de {} elementos.", batchInputs.size());
        } catch (Exception e) {
            LOG.warn(">>> [ENRIQUECIMIENTO] Entrada no es un lote JSON. Procesando como elemento único.");
            batchInputs = List.of(inputJson);
        }

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // Usamos BATCH para colecciones acotadas
        env.setRuntimeMode(RuntimeExecutionMode.BATCH);
        env.setParallelism(1);

        final int totalItems = batchInputs.size();

        // 1. Fuente: Todos los elementos del lote
        DataStream<String> sourceStream = env.fromCollection(batchInputs);

        // 2. Procesamiento y Agregación Determinista
        sourceStream
                .process(new ProcessFunction<String, String>() {
                    private transient List<String> results;
                    private transient int count;
                    private transient EnrichmentMapFunction enrichmentFn;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        results = new ArrayList<>();
                        count = 0;
                        enrichmentFn = new EnrichmentMapFunction();
                        enrichmentFn.open(parameters);
                    }

                    @Override
                    public void processElement(String value, Context ctx, Collector<String> out) throws Exception {
                        // Enriquecer
                        String enriched = enrichmentFn.map(value);
                        if (enriched != null) {
                            results.add(enriched);
                        }

                        // Contar (detectar fin de lote de forma determinista)
                        count++;

                        if (count >= totalItems) {
                            if (!results.isEmpty()) {
                                String output = MAPPER.writeValueAsString(results);
                                LOG.info(">>> [ENRIQUECIMIENTO] Lote de {}/{} completado. Disparando PERSISTENCIA...",
                                        results.size(), totalItems);
                                JobOrchestrator.triggerMongoSink(output);
                            } else {
                                LOG.warn(">>> [ENRIQUECIMIENTO] Lote final vacío (todo filtrado).");
                            }
                            results.clear();
                            count = 0;
                        }
                    }

                    @Override
                    public void close() throws Exception {
                        if (enrichmentFn != null)
                            enrichmentFn.close();
                    }
                })
                .name("EnrichmentAndBatchTrigger");

        env.execute("ApiEnrichmentJob (Indexed Batch)");
        LOG.info(">>> [ENRIQUECIMIENTO] Proceso finalizado.");
    }
}
