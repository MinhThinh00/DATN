package com.example.ShoesShop.Services.impl;


import com.example.ShoesShop.Entity.Inventory;
import com.example.ShoesShop.Repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public boolean checkInventory(Long variantId, Integer quantity) {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByVariantId(variantId);

        if (inventoryOptional.isPresent()) {
            Inventory inventory = inventoryOptional.get();
            return inventory.getQuantity() >= quantity;
        }

        return false;
    }

    @Transactional
    public void decreaseInventory(Long variantId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for variant: " + variantId));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Not enough inventory for variant: " + variantId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void increaseInventory(Long variantId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByVariantId(variantId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for variant: " + variantId));

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }
}