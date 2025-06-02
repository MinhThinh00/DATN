package com.example.ShoesShop.DTO.Report;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusReportDTO {
    private List<Long> storeIds;
    private long totalOrders;
    private long pendingConfirmation;
    private long handedOverToShipper;
    private long completed;
}