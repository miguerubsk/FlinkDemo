package org.myorg.quickstart.tasks;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.myorg.quickstart.orchestrator.ConfigLoader;
import org.apache.flink.connector.mongodb.sink.MongoSink;
import org.apache.flink.connector.mongodb.sink.writer.context.MongoSinkContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.bson.BsonDocument;

public class MongoSinkTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MongoSinkTask.class);

    public static void sink(DataStream<String> inputStream) {

        final String MONGO_URI = ConfigLoader.getEnv("MONGO_URI", null);
        final String MONGO_DB = ConfigLoader.getEnv("MONGO_DB", null);
        final String MONGO_COLLECTION = ConfigLoader.getEnv("MONGO_COLLECTION", null);
        final String MONGO_HISTORY_COLLECTION = ConfigLoader.getEnv("MONGO_HISTORY_COLLECTION", null);

        LOG.info("DEBUG ENV KEYS: " + System.getenv().keySet().toString());
        LOG.info("DEBUG ENV MONGO_HISTORY_COLLECTION: " + System.getenv("MONGO_HISTORY_COLLECTION"));

        LOG.info("Configuring Mongo Sink to: {} / {}", MONGO_URI, MONGO_DB);
        LOG.info("Collections: Main={}, History={}", MONGO_COLLECTION, MONGO_HISTORY_COLLECTION);

        // Imprimir para debug en consola
        inputStream.print("FINAL_PARA_MONGO >");

        // Sink principal (Upsert)
        MongoSink<String> sink = MongoSink.<String>builder()
                .setUri(MONGO_URI)
                .setDatabase(MONGO_DB)
                .setCollection(MONGO_COLLECTION)
                .setSerializationSchema((String input, MongoSinkContext context) -> {
                    LOG.info("Persisting (Main): {}", input);
                    BsonDocument doc = BsonDocument.parse(input);
                    // Upsert basado en "siniestro_id"
                    if (doc.containsKey("siniestro_id")) {
                        int idSiniestro = doc.getInt32("siniestro_id").getValue();
                        return new ReplaceOneModel<>(Filters.eq("siniestro_id", idSiniestro), doc,
                                new ReplaceOptions().upsert(true));
                    } else {
                        // Fallback por si acaso no tiene id_siniestro
                        return new ReplaceOneModel<>(Filters.eq("_id", doc.get("_id")), doc,
                                new ReplaceOptions().upsert(true));
                    }
                }).build();

        inputStream.sinkTo(sink).name("Mongo Sink");

        // Sink de histórico (Insert)
        if (MONGO_HISTORY_COLLECTION != null && !MONGO_HISTORY_COLLECTION.isEmpty()) {
            MongoSink<String> historySink = MongoSink.<String>builder()
                    .setUri(MONGO_URI)
                    .setDatabase(MONGO_DB)
                    .setCollection(MONGO_HISTORY_COLLECTION)
                    .setSerializationSchema((String input, MongoSinkContext context) -> {
                        LOG.info("Persisting (History): {}", input);
                        BsonDocument doc = BsonDocument.parse(input);

                        // Preservar el _id original
                        if (doc.containsKey("siniestro_id")) {
                            doc.append("original_id", doc.get("siniestro_id"));
                        }

                        // Remover _id para que Mongo genere uno nuevo y único para el histórico
                        doc.remove("siniestro_id");
                        doc.remove("_id");

                        // Añadir timestamp de histórico
                        doc.append("history_timestamp", new org.bson.BsonDateTime(System.currentTimeMillis()));

                        return new com.mongodb.client.model.InsertOneModel<>(doc);
                    }).build();

            inputStream.sinkTo(historySink).name("Mongo History Sink");
        }
    }
}
