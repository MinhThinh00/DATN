package com.example.ShoesShop.Repository;


import com.example.ShoesShop.Entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findByProductId(Long productId);
    Optional<ProductOption> findByProductIdAndOptionId(Long productId, Long optionId);
    Optional<ProductOption> findByProductIdAndOptionIdAndValue(Long productId, Long optionId, String value);
    void deleteByProductId(Long productId);
}
