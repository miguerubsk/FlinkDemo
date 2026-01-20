package org.myorg.quickstart.tasks;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.myorg.quickstart.functions.EnrichmentMapFunction;
import java.util.Objects;

public class EnrichmentTask {

    public static DataStream<String> process(DataStream<String> inputStream) {
        return inputStream
                .map(new EnrichmentMapFunction())
                .filter(Objects::nonNull);
    }
}
