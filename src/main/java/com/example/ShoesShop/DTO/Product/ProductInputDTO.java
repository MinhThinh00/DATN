package com.example.ShoesShop.DTO.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductInputDTO {
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Long categoryId;
    private Long storeId;
    private List<Long> productGroupIds;
    private List<String> images;
    private List<OptionInputDTO> options;
    private List<VariantInputDTO> variants;
}
