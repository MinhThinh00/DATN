package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByVariantId(Long variantId);
    Optional<Inventory> findByVariantIdAndStoreId(Long variantId, Long storeId);
}
