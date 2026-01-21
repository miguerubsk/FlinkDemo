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

    public static void sink(DataStream<String> inputStream) {

        final String MONGO_URI = ConfigLoader.getEnv("MONGO_URI", null);
        final String MONGO_DB = ConfigLoader.getEnv("MONGO_DB", null);
        final String MONGO_COLLECTION = ConfigLoader.getEnv("MONGO_COLLECTION", null);

        // Imprimir para debug en consola
        inputStream.print("FINAL_PARA_MONGO >");

        MongoSink<String> sink = MongoSink.<String>builder().setUri(MONGO_URI).setDatabase(MONGO_DB)
                .setCollection(MONGO_COLLECTION).setSerializationSchema((String input, MongoSinkContext context) -> {
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

        inputStream.sinkTo(sink);
    }
}
