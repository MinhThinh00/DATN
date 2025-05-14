package com.example.ShoesShop.DTO;
import lombok.Data;


@Data
public class UserUpdateDTO {
    private String fullName;
    private String phone;
    private String img;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

}
