package com.mycompany.evmc.controller;


import com.mycompany.evmc.dto.LoginDto;
import com.mycompany.evmc.dto.RegisterDto;
import com.mycompany.evmc.dto.UserProfileDto;
import com.mycompany.evmc.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint to register a new user.
     *
     * @param registerDto The registration details provided by the client.
     * @return ResponseEntity containing the created user details.
     */
    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody RegisterDto registerDto) {
        return ResponseEntity.ok(authService.registerUser(registerDto));
    }

    /**
     * Endpoint to log in a user and return a JWT token.
     *
     * @param loginDto The login credentials provided by the client.
     * @return ResponseEntity containing the JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginDto loginDto) {
        String token = authService.loginUser(loginDto);

        // Return JWT token in response body
        return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
    }
}