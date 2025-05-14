package com.example.ShoesShop.DTO;

import com.example.ShoesShop.Enum.GroupType;

import lombok.Data;

@Data
public class ProductGroupDTO {
    private Long id;
    private String name;
    private GroupType type;
    private int productCount;

}
