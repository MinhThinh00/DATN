package com.example.ShoesShop.DTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartDetailDTO {
    private Long id;
    private Long cartId;
    private Long variantId;
    private String variantName;
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

}
