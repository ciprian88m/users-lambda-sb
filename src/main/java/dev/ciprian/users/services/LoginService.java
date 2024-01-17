package dev.ciprian.users.services;

import com.amazonaws.xray.AWSXRay;
import dev.ciprian.users.config.CognitoProperties;
import dev.ciprian.users.models.AccessResponse;
import dev.ciprian.users.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.Map;

import static dev.ciprian.users.constants.UserConstants.AUTH_PASSWORD;
import static dev.ciprian.users.constants.UserConstants.AUTH_USERNAME;

@Service
public class LoginService {

    private final Logger log;
    private final CognitoProperties cognitoProperties;
    private final CognitoIdentityProviderClient identityProviderClient;

    public LoginService(CognitoProperties cognitoProperties, CognitoIdentityProviderClient identityProviderClient) {
        this.log = LoggerFactory.getLogger(this.getClass());
        this.cognitoProperties = cognitoProperties;
        this.identityProviderClient = identityProviderClient;
    }

    @NonNull
    public AccessResponse login(User user) {
        try (var loginSubsegment = AWSXRay.beginSubsegment("Login user")) {
            loginSubsegment.putAnnotation("username", user.username());

            try {
                var parameters = Map.of(AUTH_USERNAME, user.username(), AUTH_PASSWORD, user.password());

                var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder()
                        .userPoolId(cognitoProperties.getUserPoolId())
                        .clientId(cognitoProperties.getClientId())
                        .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                        .authParameters(parameters)
                        .build();

                log.info("Attempting login for user: {}", user.username());
                var adminInitiateAuthResponse = identityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);

                var authResult = adminInitiateAuthResponse.authenticationResult();
                Assert.notNull(authResult, "Authentication result cannot be null");

                var accessResponse = new AccessResponse();
                accessResponse.setTokenType(authResult.tokenType());
                accessResponse.setExpiresInSeconds(authResult.expiresIn());
                accessResponse.setAccessToken(authResult.accessToken());
                accessResponse.setRefreshToken(authResult.refreshToken());
                accessResponse.setIdToken(authResult.idToken());
                return accessResponse;
            } catch (IllegalArgumentException exception) {
                log.warn("Received null authentication result");
                loginSubsegment.addException(exception);
                return new AccessResponse(false, "Could not login user");
            } catch (SdkServiceException exception) {
                log.warn("Could not login user: {}", exception.getMessage());
                loginSubsegment.addException(exception);
                return new AccessResponse(false, "Could not login user");
            } finally {
                loginSubsegment.end();
            }
        }
    }
}
