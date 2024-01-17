package dev.ciprian.users.services;

import com.amazonaws.xray.AWSXRay;
import dev.ciprian.users.config.CognitoProperties;
import dev.ciprian.users.models.GenericResponse;
import dev.ciprian.users.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.ArrayList;

import static dev.ciprian.users.constants.UserConstants.*;

@Service
public class RegisterService {

    private final Logger log;
    private final CognitoProperties cognitoProperties;
    private final CognitoIdentityProviderClient identityProviderClient;

    public RegisterService(CognitoProperties cognitoProperties, CognitoIdentityProviderClient identityProviderClient) {
        this.log = LoggerFactory.getLogger(this.getClass());
        this.cognitoProperties = cognitoProperties;
        this.identityProviderClient = identityProviderClient;
    }

    @NonNull
    public GenericResponse createUser(User user) {
        try (var createUserSubsegment = AWSXRay.beginSubsegment("Create user")) {
            createUserSubsegment.putAnnotation("username", user.username());

            try {
                var attributes = new ArrayList<AttributeType>();
                attributes.add(AttributeType.builder().name(EMAIL).value(user.email()).build());

                if (user.firstName() != null) {
                    attributes.add(AttributeType.builder().name(GIVEN_NAME).value(user.firstName()).build());
                }

                if (user.lastName() != null) {
                    attributes.add(AttributeType.builder().name(FAMILY_NAME).value(user.lastName()).build());
                }

                var adminCreateUserRequest = AdminCreateUserRequest.builder()
                        .userPoolId(cognitoProperties.getUserPoolId())
                        .username(user.username())
                        .userAttributes(attributes)
                        .messageAction(MessageActionType.SUPPRESS)
                        .build();

                identityProviderClient.adminCreateUser(adminCreateUserRequest);
                log.info("Created user with username: {}", user.username());
                return new GenericResponse(true);
            } catch (SdkServiceException exception) {
                log.warn("Could not create user: {}", exception.getMessage());
                createUserSubsegment.addException(exception);
                return new GenericResponse(false, "Could not create user");
            } finally {
                createUserSubsegment.end();
            }
        }
    }

    @NonNull
    public GenericResponse setUserPassword(User user) {
        try (var setPasswordSubsegment = AWSXRay.beginSubsegment("Set user password")) {
            setPasswordSubsegment.putAnnotation("username", user.username());

            try {
                var adminSetUserPasswordRequest = AdminSetUserPasswordRequest.builder()
                        .userPoolId(cognitoProperties.getUserPoolId())
                        .username(user.username())
                        .password(user.password())
                        .permanent(true)
                        .build();

                identityProviderClient.adminSetUserPassword(adminSetUserPasswordRequest);
                log.info("Set user password for username: {}", user.username());
                return new GenericResponse(true);
            } catch (SdkServiceException exception) {
                log.warn("Could not set user password: {}", exception.getMessage());
                setPasswordSubsegment.addException(exception);
                return new GenericResponse(false, "Could not set user password");
            } finally {
                setPasswordSubsegment.end();
            }
        }
    }

    @NonNull
    public GenericResponse confirmUserEmail(User user) {
        try (var confirmEmailSubsegment = AWSXRay.beginSubsegment("Confirm user email")) {
            confirmEmailSubsegment.putAnnotation("username", user.username());

            try {
                var adminUpdateUserAttributesRequest = AdminUpdateUserAttributesRequest.builder()
                        .userPoolId(cognitoProperties.getUserPoolId())
                        .username(user.username())
                        .userAttributes(AttributeType.builder().name(EMAIL_VERIFIED).value("true").build())
                        .build();

                identityProviderClient.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
                log.info("User email confirmed: {}", user.username());
                return new GenericResponse(true);
            } catch (SdkServiceException exception) {
                log.warn("Could not confirm user email: {}", exception.getMessage());
                confirmEmailSubsegment.addException(exception);
                return new GenericResponse(false, "Could not confirm user email");
            } finally {
                confirmEmailSubsegment.end();
            }
        }
    }
}
