package com.example.ShoesShop.DTO.Auth;

import com.example.ShoesShop.Enum.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffDto {
    private String email;
    private String userName;
    private String password;
    private Long storeId;
    private RoleName roleName;
}
