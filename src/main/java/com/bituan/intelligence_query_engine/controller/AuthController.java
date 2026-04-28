package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.model.response.AuthResponse;
import com.bituan.intelligence_query_engine.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/github")
    public ResponseEntity<String> requestOAuthUrl () {
        return new ResponseEntity<>(authService.initializeGitHubOAuth(), HttpStatus.OK);
    }

    @GetMapping("/github/callback")
    public ResponseEntity<AuthResponse> handleCallback (@RequestParam("code") String code, @RequestParam(value = "verifier", required = false) String verifier) {
        return new ResponseEntity<>(authService.signIn(code, verifier), HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (@RequestBody Map<String, String> body) {
        return new ResponseEntity<>(authService.refresh(body.get("refresh_token")), HttpStatus.OK);
    }
}
