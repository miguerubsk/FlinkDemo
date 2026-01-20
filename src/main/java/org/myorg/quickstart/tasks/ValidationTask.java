package org.myorg.quickstart.tasks;

import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.myorg.quickstart.functions.ValidationAsyncFunction;

import java.util.concurrent.TimeUnit;

public class ValidationTask {

    public static DataStream<String> validate(DataStream<String> inputStream) {
        // Validación Async con Mockoon
        return AsyncDataStream.unorderedWait(
                inputStream, 
                new ValidationAsyncFunction(), 
                5000, TimeUnit.MILLISECONDS, 20
        );
    }
}
