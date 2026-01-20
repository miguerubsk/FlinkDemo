package org.myorg.quickstart.jobs;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;
import org.myorg.quickstart.triggers.CountOrTimeoutTrigger;
import org.myorg.quickstart.tasks.CdcSourceTask;
import org.myorg.quickstart.orchestrator.JobOrchestrator;

import java.util.ArrayList;
import java.util.List;

public class CdcIngestionJob {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CdcIngestionJob.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    public static void main(String[] args) throws Exception {
        LOG.info(">>> [CDC] Iniciando motor de escucha continua (Entorno Remoto)...");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment("localhost", 8081,
                "target/quickstart-0.1.jar");
        env.setParallelism(1);

        String[] tables = { "public.siniestros", "public.clientes", "public.expedientes", "public.servicios" };
        LOG.info(">>> [CDC] Monitorizando: {}", String.join(", ", tables));

        DataStreamSource<String> stream = CdcSourceTask.create(env, "flink_cdc_slot_ingestion", tables);

        // CONFIGURACIÓN DE LOTES: 50 elementos o 30 segundos
        stream
                .windowAll(GlobalWindows.create())
                .trigger(CountOrTimeoutTrigger.of(50, 30000))
                .process(
                        new ProcessAllWindowFunction<String, String, GlobalWindow>() {
                            @Override
                            public void process(Context context, Iterable<String> elements, Collector<String> out)
                                    throws Exception {
                                List<String> batch = new ArrayList<>();
                                for (String element : elements) {
                                    batch.add(element);
                                }

                                if (!batch.isEmpty()) {
                                    String jsonBatch = MAPPER.writeValueAsString(batch);
                                    LOG.info(
                                            ">>> [CDC BATCH] Lote de {} eventos disparado (Tamaño o Tiempo). Enviando a Enriquecimiento...",
                                            batch.size());
                                    JobOrchestrator.triggerEnrichment(jsonBatch);
                                    out.collect(jsonBatch);
                                }
                            }
                        })
                .name("RefinedBatchProcessor");

        LOG.info(">>> [CDC] Desplegando grafo en el cluster...");
        env.execute("CdcIngestionJob (Continuous-Refined-Batch)");
    }
}
