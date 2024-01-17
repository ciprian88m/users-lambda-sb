package dev.ciprian.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfig {

    private final CognitoProperties cognitoProperties;

    public CognitoConfig(CognitoProperties cognitoProperties) {
        this.cognitoProperties = cognitoProperties;
    }

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(cognitoProperties.getRegion()))
                .build();
    }
}
