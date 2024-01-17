package dev.ciprian.users.controllers;

import dev.ciprian.users.models.AccessResponse;
import dev.ciprian.users.models.GenericResponse;
import dev.ciprian.users.models.User;
import dev.ciprian.users.services.LoginService;
import dev.ciprian.users.services.RegisterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final RegisterService registerService;
    private final LoginService loginService;

    public UserController(RegisterService registerService, LoginService loginService) {
        this.registerService = registerService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(@RequestBody @Valid User user) {
        var response = registerService.createUser(user);

        if (response.isNotValid()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        response = registerService.setUserPassword(user);

        if (response.isNotValid()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        response = registerService.confirmUserEmail(user);

        if (response.isNotValid()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        response = loginService.login(user);

        if (response.isNotValid()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AccessResponse> login(@RequestBody @Valid User user) {
        var response = loginService.login(user);

        if (response.isNotValid()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
