package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class VariantOptionMapping {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

}
