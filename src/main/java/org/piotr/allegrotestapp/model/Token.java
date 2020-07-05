package org.piotr.allegrotestapp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Token {

    private String access_token = "";
    private String token_type = "";
    private String refresh_token = "";
    private long expires_in = 0;
    private String scope = "";
    private boolean allegro_api = false;
    private String jti = "";

    @Override
    public String toString() {
        return "Token : " + System.lineSeparator() +
                "access_token last 10 characters='" + access_token.substring(access_token.length()-10) + '\'' + System.lineSeparator() +
                "token_type='" + token_type + '\'' + System.lineSeparator() +
                "refresh_token last 10 characters='" + refresh_token.substring(refresh_token.length()-10) + '\'' + System.lineSeparator() +
                "expires_in=" + expires_in + System.lineSeparator() +
                "scope='" + scope + '\'' + System.lineSeparator() +
                "allegro_api=" + allegro_api + System.lineSeparator() +
                "jti='" + jti + '\'' + System.lineSeparator();
    }

}
