package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.Controller.Api.AuthApi;
import com.example.ShoesShop.DTO.Auth.LoginDto;
import com.example.ShoesShop.DTO.Auth.LoginResponse;
import com.example.ShoesShop.DTO.Auth.RegisterDto;
import com.example.ShoesShop.DTO.Auth.VerifyUserDto;
import com.example.ShoesShop.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class authImpl implements AuthApi{
    @Autowired
    private final AuthService authService;

    public authImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<?> register(RegisterDto registerRequest) {
        return authService.register(registerRequest);
    }

    @Override
    public LoginResponse login(LoginDto loginDto) {
        return authService.login(loginDto);
    }

    @Override
    public ResponseEntity<LoginResponse> loginSuccess(OAuth2AuthenticationToken token) {
        LoginResponse response = authService.loginWithGoogle(token);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        return authService.forgotPassword(email);
    }

    @Override
    public ResponseEntity<?> verifyEmail(VerifyUserDto verifyUserDto) {
        return authService.verifyEmail(verifyUserDto);
    }
}
