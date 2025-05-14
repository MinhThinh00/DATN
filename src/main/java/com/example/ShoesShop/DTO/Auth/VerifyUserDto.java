package com.example.ShoesShop.DTO.Auth;

import lombok.Data;

@Data
public class VerifyUserDto {
    private String email;
    private String otp;
}
