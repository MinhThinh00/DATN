package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
@Entity
public class ProductOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String value;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Quan hệ many-to-many đã được thay thế bằng bảng mapping VariantOptionMapping
    @OneToMany(mappedBy = "productOption")
    private List<VariantOptionMapping> variantMappings;

}
