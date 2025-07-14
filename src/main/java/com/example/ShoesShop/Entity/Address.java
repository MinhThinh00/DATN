package com.example.ShoesShop.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = {"user", "orders"})
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String phone;

    private String province;
    private String district;
    private String ward;
    private String address;

    private boolean isDefault;

}