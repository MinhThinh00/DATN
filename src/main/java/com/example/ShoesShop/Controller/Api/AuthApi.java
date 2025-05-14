package com.example.ShoesShop.Controller.Api;
import com.example.ShoesShop.DTO.Auth.LoginDto;
import com.example.ShoesShop.DTO.Auth.LoginResponse;
import com.example.ShoesShop.DTO.Auth.RegisterDto;
import com.example.ShoesShop.DTO.Auth.VerifyUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public interface AuthApi {
    @PostMapping(value = "/register")
    ResponseEntity<?> register(@RequestBody RegisterDto registerRequest);

    @PostMapping(value = "/login")
    LoginResponse login(@RequestBody LoginDto loginRequest);

    @GetMapping("/loginGoogle")
    ResponseEntity<LoginResponse> loginSuccess(OAuth2AuthenticationToken token);

    @PostMapping("/forgot_password")
    ResponseEntity<?> forgotPassword(@RequestBody String email);


    @PostMapping (value = "/verify")
    ResponseEntity<?> verifyEmail(@RequestBody VerifyUserDto verifyUserDto);

}