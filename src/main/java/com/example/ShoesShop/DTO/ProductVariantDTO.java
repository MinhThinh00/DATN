package com.example.ShoesShop.DTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantDTO {
    private Long id;
    private String productName;
    private String name;
    private String sku;
    private BigDecimal price;
    private String img;
    private Integer quantity;
    private List<Long> optionIds;

}
