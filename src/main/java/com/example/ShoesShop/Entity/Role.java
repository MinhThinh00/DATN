package com.example.ShoesShop.Entity;
import com.example.ShoesShop.Enum.RoleName;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleName roleName;
}



