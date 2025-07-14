package com.example.ShoesShop.DTO.Report;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryProductCountDTO {
    private Long categoryId;
    private String categoryName;
    private long productCount;
}