package dev.ciprian.users.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessResponse extends GenericResponse {

    private String tokenType;
    private Integer expiresInSeconds;
    private String accessToken;
    private String refreshToken;
    private String idToken;

    public AccessResponse() {
        super(true);
    }

    public AccessResponse(boolean valid) {
        super(valid);
    }

    public AccessResponse(boolean valid, String message) {
        super(valid, message);
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Integer expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
