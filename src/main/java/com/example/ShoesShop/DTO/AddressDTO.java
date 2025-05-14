package com.example.ShoesShop.DTO;
import lombok.Data;


@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String phone;
    private String address;
    private boolean isDefault;

}
