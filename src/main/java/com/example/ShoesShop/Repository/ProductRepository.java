package com.example.ShoesShop.Repository;


import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Enum.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStoreId(Long storeId);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p JOIN p.groupMappings gm JOIN gm.productGroup pg WHERE pg.type = :groupType")
    Page<Product> findByGroupType(@Param("groupType") GroupType groupType, Pageable pageable);
}