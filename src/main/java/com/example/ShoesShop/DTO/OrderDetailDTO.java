package com.example.ShoesShop.DTO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class                                                                                                                    OrderDetailDTO {
    private Long id;
    private ProductVariantDTO variant;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

}
