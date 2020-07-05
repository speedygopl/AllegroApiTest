package org.piotr.allegrotestapp.service;

import org.piotr.allegrotestapp.enums.AllegroEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Service
public class WebClientServiceIm implements WebClientService {

    @Autowired
    AllegroJwtToken allegroJwtToken;

    @Override
    public WebClient plainGetRequest() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("ACCEPT", AllegroEnum.ACCEPT.acceptHeader)
                .build();
    }

    @Override
    public WebClient httpGetRequestWitToken() throws IOException {
        return WebClient.builder()
                .baseUrl("https://api.allegro.pl.allegrosandbox.pl")
                .defaultHeader("ACCEPT", AllegroEnum.ACCEPT.acceptHeader)
                .defaultHeader("Authorization", "Bearer " + allegroJwtToken.getAllegroJwtToken().getAccess_token())
                .defaultHeader("Content-Type", AllegroEnum.ACCEPT.acceptHeader)
                .build();
    }

}
