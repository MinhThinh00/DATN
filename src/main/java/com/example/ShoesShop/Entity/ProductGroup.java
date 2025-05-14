package com.example.ShoesShop.Entity;
import com.example.ShoesShop.Enum.GroupType;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class ProductGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private GroupType type;  // ENUM: NAM, NU, TRE_EM

    @OneToMany(mappedBy = "productGroup", cascade = CascadeType.ALL)
    private List<ProductGroupMapping> productMappings;

}
