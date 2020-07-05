package org.piotr.allegrotestapp.service;

import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public interface WebClientService {

    public WebClient plainGetRequest();

    WebClient httpGetRequestWitToken() throws IOException;
}
