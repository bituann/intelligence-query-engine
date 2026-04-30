package com.bituan.intelligence_query_engine.controller;

import com.bituan.intelligence_query_engine.model.response.UserResponse;
import com.bituan.intelligence_query_engine.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser (@AuthenticationPrincipal UserDetails authUser) {
        return new ResponseEntity<>(authService.getUser(authUser.getUsername()), HttpStatus.OK);
    }
}
