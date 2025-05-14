package com.example.ShoesShop.DTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private List<CartDetailDTO> items;
    private BigDecimal totalAmount;
    private int itemCount;
    private LocalDateTime updatedAt;

}
