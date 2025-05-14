package com.example.ShoesShop.DTO.Product;

import lombok.Data;

import java.util.List;
@Data
public class OptionInputDTO {
    private String name;
    private List<String> values;
}
