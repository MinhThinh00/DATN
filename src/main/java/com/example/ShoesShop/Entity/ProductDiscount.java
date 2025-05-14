package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ProductDiscount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

}
