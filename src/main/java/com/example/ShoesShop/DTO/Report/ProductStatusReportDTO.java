package com.example.ShoesShop.DTO.Report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatusReportDTO {
    private List<Long> storeIds;
    private long totalProducts;
    private long outOfStock;
    private long lowStock;
    private List<CategoryProductCountDTO> categories;
}