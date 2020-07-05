package org.piotr.allegrotestapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.piotr.allegrotestapp.model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
public class AllegroJwtToken {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${clientId}")
    @Getter
    private String clientId = "";
    @Value("${clientSecret}")
    @Getter
    private String clientSecret = "";
    //@Value("${authCode=code}")
    private String authCode = "";
    private Token allegroJwtToken = null;
    ObjectMapper objectMapper = new ObjectMapper();


    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getAuthCode() {
        return authCode;
    }

    public Token getAllegroJwtToken() throws IOException {
        allegroJwtToken = objectMapper.readValue(new File("token.txt"), Token.class);
        return allegroJwtToken;
    }

    public void setAllegroJwtToken(Token allegroJwtToken) {
        this.allegroJwtToken = allegroJwtToken;
    }

    public void getAuthCode(String clientId) {
        WebClient
                .builder()
                .baseUrl("https://allegro.pl.allegrosandbox.pl/auth/oauth/authorize?response_type=code&client_id=" + clientId)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();

    }


    public String requestJWT() {
        String url = "https://allegro.pl.allegrosandbox.pl/auth/oauth/token?grant_type=authorization_code&code=" + authCode;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
        String body = "";
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        return responseEntity.getBody();

    }

    @Override
    public String toString() {
        return "AllegroJwtToken{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", authCode='" + authCode + '\'' +
                ", allegroJwtToken=" + allegroJwtToken +
                '}';
    }
}
