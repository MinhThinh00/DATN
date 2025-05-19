package com.example.ShoesShop.DTO.Report;

import java.math.BigDecimal;

public class CategoryRevenueDTO {
    private String name;
    private BigDecimal value;

    public CategoryRevenueDTO(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}

