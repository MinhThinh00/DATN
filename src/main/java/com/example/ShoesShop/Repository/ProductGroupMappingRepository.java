package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.ProductGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductGroupMappingRepository extends JpaRepository<ProductGroupMapping, Long> {
    List<ProductGroupMapping> findByProductId(Long productId);
    List<ProductGroupMapping> findByProductGroupId(Long productGroupId);
    Optional<ProductGroupMapping> findByProductIdAndProductGroupId(Long productId, Long productGroupId);
    void deleteByProductId(Long productId);
}