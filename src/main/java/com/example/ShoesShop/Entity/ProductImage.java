package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String imageURL;
    private boolean isDefault;

}
