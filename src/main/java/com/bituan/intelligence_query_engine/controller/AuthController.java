package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.model.response.AuthResponse;
import com.bituan.intelligence_query_engine.model.response.UserResponse;
import com.bituan.intelligence_query_engine.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    @Value("${FRONTEND_AUTH_CALLBACK}")
    private String callback;

    private final AuthService authService;

    @GetMapping("/github")
    public ResponseEntity<String> requestOAuthUrl () {
        return new ResponseEntity<>(authService.initializeGitHubOAuth(), HttpStatus.OK);
    }

    @GetMapping("/github/callback")
    public ResponseEntity<AuthResponse> handleCallback (@RequestParam(name = "code") String code, @RequestParam(name = "code_verifier", required = false) String verifier) {
        AuthResponse response = authService.signIn(code, verifier);

        if (verifier != null && !verifier.isBlank()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Web flow - set cookies directly, redirect
        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", response.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .build();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.LOCATION, callback)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (@RequestBody Map<String, String> body) {
        return new ResponseEntity<>(authService.refresh(body.get("refresh_token")), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@AuthenticationPrincipal UserDetails authUser) {
        authService.logout(authUser.getUsername());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser (@AuthenticationPrincipal UserDetails authUser) {
        return new ResponseEntity<>(authService.getUser(authUser.getUsername()), HttpStatus.OK);
    }
}
