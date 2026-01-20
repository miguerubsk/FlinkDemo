package org.myorg.quickstart.tasks;

import com.ververica.cdc.connectors.base.options.StartupOptions;
import com.ververica.cdc.connectors.postgres.source.PostgresSourceBuilder;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.myorg.quickstart.orchestrator.ConfigLoader;

public class CdcSourceTask {

    private static final String[] DEFAULT_TABLES = {
            "public.siniestros",
            "public.clientes",
            "public.expedientes",
            "public.servicios"
    };

    public static DataStreamSource<String> create(StreamExecutionEnvironment env) {
        return create(env, "flink_cdc_slot_demo", DEFAULT_TABLES);
    }

    public static DataStreamSource<String> create(StreamExecutionEnvironment env, String... tables) {
        return create(env, "flink_cdc_slot_demo", tables);
    }

    public static DataStreamSource<String> create(StreamExecutionEnvironment env, String slotName, String... tables) {
        var postgresIncrementalSource = PostgresSourceBuilder.PostgresIncrementalSource.<String>builder()
                .hostname(ConfigLoader.getEnv("POSTGRES_HOST", "postgres_cdc"))
                .port(Integer.parseInt(ConfigLoader.getEnv("POSTGRES_PORT", "5432")))
                .database(ConfigLoader.getEnv("POSTGRES_DB", "mi_base_datos"))
                .schemaList("public")
                .tableList(tables)
                .username(ConfigLoader.getEnv("POSTGRES_USER", "usuario_flink"))
                .password(ConfigLoader.getEnv("POSTGRES_PASSWORD", "password_flink"))
                .decodingPluginName("pgoutput")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .startupOptions(StartupOptions.latest())
                .slotName(slotName)
                .build();

        return env.fromSource(postgresIncrementalSource, WatermarkStrategy.noWatermarks(), "PostgresCDC-" + slotName);
    }
}
