package dev.ciprian.users.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ciprian.users.models.AccessResponse;
import dev.ciprian.users.models.GenericResponse;
import dev.ciprian.users.models.User;
import dev.ciprian.users.services.LoginService;
import dev.ciprian.users.services.RegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class})
class UserControllerTest {

    @MockBean
    RegisterService registerService;

    @MockBean
    LoginService loginService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Register user works")
    void test_0() throws Exception {
        when(registerService.createUser(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.setUserPassword(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.confirmUserEmail(any(User.class))).thenReturn(new GenericResponse(true));
        when(loginService.login(any(User.class))).thenReturn(getAccessResponse());

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNotEmpty())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.idToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Register user returns error message if an operation fails (1)")
    void test_1() throws Exception {
        when(registerService.createUser(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.setUserPassword(any(User.class))).thenReturn(new GenericResponse(false, "Could not set user password"));

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessages").isNotEmpty())
                .andExpect(jsonPath("$.errorMessages[0]").value("Could not set user password"));
    }

    @Test
    @DisplayName("Register user returns error message if an operation fails (2)")
    void test_2() throws Exception {
        when(registerService.createUser(any(User.class))).thenReturn(new GenericResponse(false, "Could not create user"));

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessages").isNotEmpty())
                .andExpect(jsonPath("$.errorMessages[0]").value("Could not create user"));
    }

    @Test
    @DisplayName("Register user returns error message if an operation fails (3)")
    void test_3() throws Exception {
        when(registerService.createUser(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.setUserPassword(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.confirmUserEmail(any(User.class))).thenReturn(new GenericResponse(false, "Could not confirm user email"));

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessages").isNotEmpty())
                .andExpect(jsonPath("$.errorMessages[0]").value("Could not confirm user email"));
    }

    @Test
    @DisplayName("Register user returns error message if an operation fails (4)")
    void test_4() throws Exception {
        when(registerService.createUser(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.setUserPassword(any(User.class))).thenReturn(new GenericResponse(true));
        when(registerService.confirmUserEmail(any(User.class))).thenReturn(new GenericResponse(true));
        when(loginService.login(any(User.class))).thenReturn(new AccessResponse(false, "Could not login user"));

        mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessages").isNotEmpty())
                .andExpect(jsonPath("$.errorMessages[0]").value("Could not login user"));
    }

    @Test
    @DisplayName("Login user works")
    void test_5() throws Exception {
        when(loginService.login(any(User.class))).thenReturn(getAccessResponse());

        mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").isNotEmpty())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.idToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Login user returns error message if an operation fails")
    void test_6() throws Exception {
        when(loginService.login(any(User.class))).thenReturn(new AccessResponse(false, "Could not login user"));

        mockMvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(getUser()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessages").isNotEmpty())
                .andExpect(jsonPath("$.errorMessages[0]").value("Could not login user"));
    }

    private AccessResponse getAccessResponse() {
        var accessResponse = new AccessResponse(true);
        accessResponse.setTokenType("access");
        accessResponse.setExpiresInSeconds(3600);
        accessResponse.setAccessToken("accessToken");
        accessResponse.setIdToken("idToken");
        accessResponse.setRefreshToken("refreshToken");
        return accessResponse;
    }

    private String getUser() throws JsonProcessingException {
        var user = new User("", "", "email", "username", "password");
        return objectMapper.writeValueAsString(user);
    }
}