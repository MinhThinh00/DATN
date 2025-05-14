package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;


@Data
@Entity
@ToString(exclude = {"cart", "productVariant"})
public class CartDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

}
