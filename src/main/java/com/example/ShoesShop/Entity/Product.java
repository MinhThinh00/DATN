package com.example.ShoesShop.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
@Data
@Entity
@ToString(exclude = {
        "store", "category", "images", "variants",
        "productOptions", "discounts", "groupMappings"
})
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private String description;
    private BigDecimal basePrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductOption> productOptions;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductDiscount> discounts;

    // Quan hệ với ProductGroup thông qua bảng ProductGroupMapping
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductGroupMapping> groupMappings;


}
