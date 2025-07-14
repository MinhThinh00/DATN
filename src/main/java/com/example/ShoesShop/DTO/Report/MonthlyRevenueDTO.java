package com.example.ShoesShop.DTO.Report;

import java.math.BigDecimal;
import java.util.Map;

public class MonthlyRevenueDTO {
    private String name;
    private Map<String, BigDecimal> storeRevenues;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, BigDecimal> getStoreRevenues() {
        return storeRevenues;
    }

    public void setStoreRevenues(Map<String, BigDecimal> storeRevenues) {
        this.storeRevenues = storeRevenues;
    }
}
