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
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {RegisterService.class})
class RegisterServiceTest {

    @MockBean
    CognitoProperties cognitoProperties;

    @MockBean
    CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Autowired
    RegisterService registerService;

    @BeforeEach
    void setUp() {
        AWSXRay.beginSegment("RegisterServiceTest");
    }

    @AfterEach
    void tearDown() {
        AWSXRay.clearTraceEntity();
    }

    @Test
    @DisplayName("Create user works")
    void test_0() {
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.createUser(user);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrorMessages()).isEmpty();
    }

    @Test
    @DisplayName("Create user returns error message if the AWS SDK fails")
    void test_1() {
        when(cognitoIdentityProviderClient.adminCreateUser(any(AdminCreateUserRequest.class))).thenThrow(SdkServiceException.class);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.createUser(user);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrorMessages().size()).isEqualTo(1);
        assertThat(response.getErrorMessages().getFirst()).isEqualTo("Could not create user");
    }

    @Test
    @DisplayName("Set user password works")
    void test_2() {
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.setUserPassword(user);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrorMessages()).isEmpty();
    }

    @Test
    @DisplayName("Set user password returns error message if the AWS SDK fails")
    void test_3() {
        when(cognitoIdentityProviderClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class))).thenThrow(SdkServiceException.class);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.setUserPassword(user);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrorMessages().size()).isEqualTo(1);
        assertThat(response.getErrorMessages().getFirst()).isEqualTo("Could not set user password");
    }

    @Test
    @DisplayName("Confirm user email works")
    void test_4() {
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.confirmUserEmail(user);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrorMessages()).isEmpty();
    }

    @Test
    @DisplayName("Confirm user email returns error message if the AWS SDK fails")
    void test_5() {
        when(cognitoIdentityProviderClient.adminUpdateUserAttributes(any(AdminUpdateUserAttributesRequest.class))).thenThrow(SdkServiceException.class);
        var user = new User("First name", "Last name", "email", "username", "password");
        var response = registerService.confirmUserEmail(user);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrorMessages().size()).isEqualTo(1);
        assertThat(response.getErrorMessages().getFirst()).isEqualTo("Could not confirm user email");
    }
}