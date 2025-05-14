package com.example.ShoesShop.DTO.Auth;

import com.example.ShoesShop.Enum.RoleName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;
    private String email;
    private String userName;
    private String img;
    private Boolean isActive;
    private int otp;
    private Timestamp otpExpiryTime;
    private Long storeId;
    private RoleName roleName;
    private Timestamp createAt;
}