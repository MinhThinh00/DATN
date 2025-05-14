package com.example.ShoesShop.Repository;


import com.example.ShoesShop.Entity.VariantOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantOptionMappingRepository extends JpaRepository<VariantOptionMapping, Long> {
    List<VariantOptionMapping> findByVariantId(Long variantId);
    void deleteByVariantId(Long variantId);

    void deleteByProductOptionId(Long optionId);
}