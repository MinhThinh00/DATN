package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Entity.ProductGroup;
import com.example.ShoesShop.Enum.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {
    Optional<ProductGroup> findByType(GroupType type);
    Optional<ProductGroup> findById(Long id);
}