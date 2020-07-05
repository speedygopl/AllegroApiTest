package org.piotr.allegrotestapp.config;

import org.piotr.allegrotestapp.enums.AllegroEnum;
import org.piotr.allegrotestapp.interceptor.HttpInterceptor;
import org.piotr.allegrotestapp.service.AllegroJwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class Beans {
    @Value("${clientId}")
    String clientId;
    @Autowired
    AllegroJwtToken allegroJwtToken;


    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new HttpInterceptor());
        return restTemplate;
    }

    @Bean
    public WebClient defaultGetRequest() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("ACCEPT", AllegroEnum.ACCEPT.acceptHeader)
                .build();
    }

}
