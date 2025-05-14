package com.example.ShoesShop.DTO;
import lombok.Data;

@Data
public class UserCreateDTO {
    private String email;
    private String password;
    private String confirmPassword;
    private String fullName;
    private String phone;

}
