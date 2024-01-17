package dev.ciprian.users.services;

import com.amazonaws.xray.AWSXRay;
import dev.ciprian.users.config.CognitoProperties;
import dev.ciprian.users.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {LoginService.class})
class LoginServiceTest {

    @MockBean
    CognitoProperties cognitoProperties;

    @MockBean
    CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Autowired
    LoginService loginService;

    @BeforeEach
    void setUp() {
        AWSXRay.beginSegment("RegisterServiceTest");
    }

    @AfterEach
    void tearDown() {
        AWSXRay.clearTraceEntity();
    }

    @Test
    @DisplayName("Login user works")
    void test_0() {
        var result = AuthenticationResultType.builder().build();
        var authResponse = AdminInitiateAuthResponse.builder().authenticationResult(result).build();
        when(cognitoIdentityProviderClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(authResponse);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = loginService.login(user);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrorMessages()).isEmpty();
    }

    @Test
    @DisplayName("Login user returns error message if auth result is not present")
    void test_1() {
        var authResponse = AdminInitiateAuthResponse.builder().authenticationResult((AuthenticationResultType) null).build();
        when(cognitoIdentityProviderClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenReturn(authResponse);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = loginService.login(user);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrorMessages().size()).isEqualTo(1);
        assertThat(response.getErrorMessages().getFirst()).isEqualTo("Could not login user");
    }

    @Test
    @DisplayName("Login user returns error message if the AWS SDK fails")
    void test_2() {
        when(cognitoIdentityProviderClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class))).thenThrow(SdkServiceException.class);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = loginService.login(user);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrorMessages().size()).isEqualTo(1);
        assertThat(response.getErrorMessages().getFirst()).isEqualTo("Could not login user");
    }
}