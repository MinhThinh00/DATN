package com.example.ShoesShop.DTO;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class DiscountDTO {
    private Long id;
    private String name;

    private String code;
    private Long quantity;

    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    //private int productCount;

}
