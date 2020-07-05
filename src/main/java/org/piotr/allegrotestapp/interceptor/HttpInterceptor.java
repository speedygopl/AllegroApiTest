package org.piotr.allegrotestapp.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class HttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("ACCEPT","application/vnd.allegro.public.v1+json");
        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }
}
