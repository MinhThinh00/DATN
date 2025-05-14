package com.example.ShoesShop.DTO;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ProductGroupMappingDTO {
    private Long id;
    private ProductGroupDTO group;
    private LocalDateTime addedDate;
}
