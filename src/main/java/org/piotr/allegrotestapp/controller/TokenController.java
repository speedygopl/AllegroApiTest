package org.piotr.allegrotestapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.piotr.allegrotestapp.model.Token;
import org.piotr.allegrotestapp.service.AllegroJwtToken;
import org.piotr.allegrotestapp.service.UuidService;
import org.piotr.allegrotestapp.service.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class TokenController {

    @Autowired
    private AllegroJwtToken allegroJwtToken;
    @Autowired
    private RestTemplate template;
    @Autowired
    WebClientService webClientService;

    List<String> allLines = new ArrayList<>();
    String result = "";
    String id = "";
    Map<String, Integer> inputMap = new HashMap<>();
    Integer updatedQuantityInStock;

    public List<String> readAllLinesFromFile() throws IOException {
        allLines = Files.readAllLines(Paths.get("c:\\AllegroApi\\plik.txt"), Charset.forName("ISO-8859-2"));
        return allLines;
    }

    public Map<String, Integer> getInputMap() throws IOException {
        allLines = readAllLinesFromFile();
        Pattern p = Pattern.compile("([ +][0-9]+[|])(.{30})( +)([|])( +)([0-9]+)");
        Matcher m;
        for (int i = 0; i < allLines.size(); i++) {
            m = p.matcher(allLines.get(i));
            if (m.find()) {
                inputMap.put(m.group(2).trim(), Integer.valueOf(m.group(6)));
            }
        }
        return inputMap;
    }


    @GetMapping("/refresh")
    public void useRefreshToken() throws IOException {
        //utworzenie mappera json
        ObjectMapper objectMapper = new ObjectMapper();
        //utworzenie obiektu tokena
        Token token = objectMapper.readValue(new File("token.txt"), Token.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "basic Zjk3ODQ5MDRlMWQyNGRhZTg3NmZjOGE2ZDM0Mzg0OTQ6SlJQNzYyQU10ejJzRElaUUlKUWdnQkU4TUc5TUQwdEl0eEhhTHVISEtVdmtXY0l4ZHRTUU4ycUpFenJVT0prcA==");
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = template.exchange("https://allegro.pl.allegrosandbox.pl/auth/oauth/token?grant_type=refresh_token&refresh_token=" + token.getRefresh_token() + "&redirect_uri=http://localhost:8080", HttpMethod.POST, requestEntity, String.class);
        String response = responseEntity.getBody();


        //utworzenie streamu dla System.Out.Println do zapisywania w pliku
        //utworzenie zmiennej do wyświetlania na konsoli
        PrintStream file = new PrintStream(new File("token.txt"));
        PrintStream console = System.out;

        //stream odpowiedzi serwera do pliku
        System.setOut(file);
        System.out.println(response);

        //przestawienie streamu na konsolę i wyświetlenie informacji o obiekcie token
        System.setOut(console);
        token = objectMapper.readValue(new File("token.txt"), Token.class);
        System.out.println(token.toString());
    }

    public JsonNode changeJson(JsonNode inputJson, Integer quantityToChange) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode changedJson = objectMapper.readValue(inputJson.toString(), JsonNode.class);
        Integer available = changedJson.get("stock").get("available").asInt();
        updatedQuantityInStock = available - quantityToChange;
        if(updatedQuantityInStock <= 0) {
            System.out.println("zamykanie aukcji");
            closeAuction();
        } else {
            ((ObjectNode) changedJson.get("stock")).put("available", updatedQuantityInStock);
        }
        return changedJson;
    }

    public JsonNode changeJsonForAuctionClose() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonAuctionCloseChanged = objectMapper.readValue(new File("auctionclosed.txt"), JsonNode.class);
        System.out.println(jsonAuctionCloseChanged);
        ((ObjectNode)jsonAuctionCloseChanged.get("offerCriteria").get(0).get("offers").get(0)).put("id", id);
        System.out.println(jsonAuctionCloseChanged);
        return jsonAuctionCloseChanged;
    }


    public void closeAuction() throws IOException {
        UuidService uuidService = new UuidService();
        String uuid = uuidService.uuid.toString();
        System.out.println(uuid);
        String jsonAuctionCloseChanged = changeJsonForAuctionClose().toString();
        webClientService.httpGetRequestWitToken()
                .put()
                .uri("/sale/offer-publication-commands/" + uuid)
                .body(BodyInserters.fromValue(jsonAuctionCloseChanged))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    @GetMapping
    public JsonNode getOfferById(String externalId) throws IOException, InterruptedException {
        JsonNode jsonByExternalId = webClientService.httpGetRequestWitToken()
                .get()
                .uri("/sale/offers?publication.status=ACTIVE&external.id=" + externalId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        id = jsonByExternalId.get("offers").get(0).get("id").toString().replaceAll("\"", "");
        JsonNode jsonById = webClientService.httpGetRequestWitToken()
                .get()
                .uri("/sale/offers/" + id)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return jsonById;
    }

    @GetMapping("/dojob")
    public void doJob() throws IOException {
        List<String> resultsSuccess = new ArrayList<>();
        List<String> resultsError = new ArrayList<>();
        List<String> resultsClosed = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : getInputMap().entrySet()) {
            try {
                JsonNode inputJson = getOfferById(entry.getKey());
                JsonNode outputJson = changeJson(inputJson, entry.getValue());
                if(updatedQuantityInStock <=0) {
                    resultsClosed.add("offer "+ entry.getKey() + " auction CLOSED!!!" + " (quantity after change = " + updatedQuantityInStock + ")");
                } else {
                    changeOfferWithOutputJson(outputJson);
                    resultsSuccess.add("offer " + entry.getKey() + " changed SUCCESSFULLY !!!" + " (quantity after change = " + updatedQuantityInStock + ")");
                }
            } catch (Exception exc) {
                resultsError.add("offer " + entry.getKey() + " - NOT CHANGED!!!" + " (quantity to change = " + entry.getValue() + ") "+ "error message : " + exc.getMessage());
            }
        }
        PrintStream file = new PrintStream(new File("c:\\AllegroApi\\outputFile.txt"));
        System.setOut(file);
        System.out.println("RESULTS QUANTITY CHANGED !!!");
        for (String s : resultsSuccess) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("RESULTS AUCTIONS CLOSED !!!");
        for (String s : resultsClosed) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("RESULTS WITH ERRORS !!!");
        for (String s : resultsError) {
            System.out.println(s);
        }
    }

    @RequestMapping
    public boolean changeOfferWithOutputJson(JsonNode outputJson) throws IOException {
        JsonNode putJson = webClientService.httpGetRequestWitToken()
                .put()
                .uri("/sale/offers/" + id)
                .body(BodyInserters.fromValue(outputJson.toString()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return true;
    }
    //TESTY----------------------------------------------------------------------------------

    @GetMapping("/getoffer")
    public JsonNode getOfferByExternalId(String externalId) throws IOException, InterruptedException {
        return webClientService.httpGetRequestWitToken()
                .get()
                .uri("/sale/offers?publication.status=ACTIVE&external.id=" + externalId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }


}
