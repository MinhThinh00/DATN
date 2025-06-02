package com.example.ShoesShop.Controller.impl;


import com.example.ShoesShop.DTO.Report.*;
import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Services.impl.ReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired
    private ReportServiceImpl reportService;

    @GetMapping("/revenue/{year}")
    public Map<String, List<MonthlyRevenueDTO>> getRevenueByYear(@PathVariable int year) {
        return reportService.getRevenueByYear(year);
    }

    @GetMapping("/revenue/category/{month}/{year}")
    public Map<String, List<Map<String, List<CategoryRevenueDTO>>>> getRevenueByCategory(
            @PathVariable int month, @PathVariable int year) {
        return reportService.getRevenueByCategory(month, year);
    }
    // Báo cáo top 10 sản phẩm bán chạy
    @GetMapping("/top-products/{month}/{year}")
    public Map<String, List<Map<String, List<TopProductDTO>>>> getTopProducts(
            @PathVariable int month, @PathVariable int year) {
        return reportService.getTopProducts(month, year);
    }
    @GetMapping("/stores/summary")
    public ResponseEntity<StoreSummaryDTO> getStoreSummary(){
        StoreSummaryDTO summary = reportService.getStoreSummary( );
        return ResponseEntity.ok(summary);
    }
    @GetMapping("/orders/status")
    public ResponseEntity<OrderStatusReportDTO> getOrderStatusReport(
            @RequestParam("storeIds") String storeIds) {
        OrderStatusReportDTO report = reportService.getOrderStatusReport(storeIds);
        return ResponseEntity.ok(report);
    }
//    @GetMapping("/products/status")
//    public ResponseEntity<ProductStatusReportDTO> getProductStatusReport(
//            @RequestParam("storeIds") String storeIds) {
//        ProductStatusReportDTO report = reportService.getProductStatusReport(storeIds);
//        return ResponseEntity.ok(report);
//    }

}
