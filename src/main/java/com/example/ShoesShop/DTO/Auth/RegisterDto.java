package com.example.ShoesShop.DTO.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {
    private String email;
    private String password;
    private String username;
    private String Role;
    private String img;
}
