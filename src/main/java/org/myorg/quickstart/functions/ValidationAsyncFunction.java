package org.myorg.quickstart.functions;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.concurrent.FutureCallback;
import java.util.Collections;

public class ValidationAsyncFunction extends RichAsyncFunction<String, String> {
    
    private transient CloseableHttpAsyncClient httpClient;

    @Override
    public void open(Configuration parameters) throws Exception {
        httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    @Override
    public void asyncInvoke(String inputJson, ResultFuture<String> resultFuture) {
        HttpPost request = new HttpPost("http://mockoon_api:3000/api/validar");
        request.setEntity(new StringEntity(inputJson, "UTF-8"));
        request.setHeader("Content-Type", "application/json");

        httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                // Verificar código HTTP
                if (result.getStatusLine().getStatusCode() == 200) {
                    // Si es válido, pasamos el mismo JSON adelante para que llegue a Mongo
                    resultFuture.complete(Collections.singleton(inputJson));
                } else {
                    // Si no es válido, no lo emitimos (o podríamos emitir a un side-output de errores)
                    System.out.println("VALIDATION FAILED: " + result.getStatusLine().getStatusCode());
                    resultFuture.complete(Collections.emptyList());
                }
            }

            @Override
            public void failed(Exception ex) {
                System.out.println("HTTP REQUEST FAILED: " + ex.getMessage());
                resultFuture.complete(Collections.emptyList());
            }

            @Override
            public void cancelled() {
                resultFuture.complete(Collections.emptyList());
            }
        });
    }
}
