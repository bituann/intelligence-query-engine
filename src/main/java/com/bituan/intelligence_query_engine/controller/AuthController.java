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

    private final AuthService authService;

    @GetMapping("/github")
    public ResponseEntity<String> requestOAuthUrl (@RequestParam(required = false, name = "state") String state,
                                                   @RequestParam(required = false, name = "redirect_uri") String redirectUri,
                                                   @RequestParam(required = false, name = "code_challenge") String codeChallenge,
                                                   @RequestParam(required = false, name = "code_challenge_method") String codeChallengeMethod) {
        return new ResponseEntity<>(authService.initializeGitHubOAuth(state, redirectUri ,codeChallenge ,codeChallengeMethod), HttpStatus.OK);
    }

    @GetMapping("/github/callback")
    public ResponseEntity<AuthResponse> handleCallback (@RequestParam(name = "code") String code, @RequestParam(name = "code_verifier", required = false) String verifier) {
        return new ResponseEntity<>(authService.signIn(code, verifier), HttpStatus.OK);
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
