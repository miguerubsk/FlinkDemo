package org.myorg.quickstart;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.myorg.quickstart.tasks.CdcSourceTask;
import org.myorg.quickstart.tasks.EnrichmentTask;
import org.myorg.quickstart.tasks.MongoSinkTask;
import org.myorg.quickstart.tasks.ValidationTask;

/**
 * ORQUESTADOR PRINCIPAL
 * Este trabajo de Flink actúa como coordinador del pipeline completo.
 * Llama secuencialmente a las distintas tareas (Source, Enriquecimiento, Validación, Sink).
 */
public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        // 1. INICIALIZACIÓN DEL ENTORNO DE FLINK
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createRemoteEnvironment(
                "localhost", 
                8081, 
                "target/quickstart-0.1.jar"
        );
        env.setParallelism(1);
        env.enableCheckpointing(5000);

        // 2. TAREA 1: CDC SOURCE (JOB 1 & 2 IMPLÍCITOS)
        DataStream<String> sourceStream = CdcSourceTask.create(env);

        // 3. TAREA 3: ENRIQUECIMIENTO (JOB 3)
        // Lógica de mapeo y lookup en base de datos
        DataStream<String> enrichedStream = EnrichmentTask.process(sourceStream);

        // 4. TAREA 4: VALIDACIÓN (JOB 4)
        // Lógica de llamada asíncrona a la API Mockoon
        DataStream<String> validatedStream = ValidationTask.validate(enrichedStream);

        // 5. TAREA 5: SINK MONGO (JOB 5)
        // Escritura final en base de datos documental
        MongoSinkTask.sink(validatedStream);

        // 6. EJECUCIÓN (El "Superior Class" lanza todo el grafo)
        env.execute("Orquestador Siniestros (Modular)");
    }
}

