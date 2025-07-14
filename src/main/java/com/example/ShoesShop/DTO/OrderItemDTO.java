package com.example.ShoesShop.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long cartId;
    private Long variantId;
    private String variantName;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String image;
    private Long productId;
}
