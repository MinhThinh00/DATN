package com.example.ShoesShop.Controller.impl;


import com.example.ShoesShop.DTO.Report.CategoryRevenueDTO;
import com.example.ShoesShop.DTO.Report.MonthlyRevenueDTO;
import com.example.ShoesShop.DTO.Report.TopProductDTO;
import com.example.ShoesShop.Services.impl.ReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


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
    public Map<String, List<TopProductDTO>> getTopProducts(
            @PathVariable int month, @PathVariable int year) {
        return reportService.getTopProducts(month, year);
    }
}
