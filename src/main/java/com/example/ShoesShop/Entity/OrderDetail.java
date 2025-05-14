package com.example.ShoesShop.Entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
@Entity
@Data
@ToString(exclude = {"order", "productVariant"})
public class OrderDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

}
