package com.example.ShoesShop.DTO;
import lombok.Data;

@Data
public class StoreDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private int productCount;
    private int staffCount;

}
