package com.example.ShoesShop.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@ToString(exclude = {"product", "inventory", "optionMappings", "cartDetails", "orderDetails"})
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sku;
    private BigDecimal price;
    private String img;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "productVariant")
    private List<CartDetail> cartDetails;

    @OneToMany(mappedBy = "productVariant")
    private List<OrderDetail> orderDetails;

    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL)
    private Inventory inventory;

    // Thay thế mối quan hệ many-to-many bằng bảng trung gian VariantOptionMapping
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private List<VariantOptionMapping> optionMappings;

}
