package com.example.ShoesShop.DTO.Report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreSummaryDTO {
    private Integer year;
    private Integer month;
    private List<Long> storeIds;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private int totalProductsSold;
    private long totalCustomers;
}