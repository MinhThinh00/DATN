package com.example.ShoesShop.DTO.Product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VariantInputDTO {
    private String sku;
    private BigDecimal price;
    private String variantImage;
    private String color;
    private List<SizeDTO> sizes;
}