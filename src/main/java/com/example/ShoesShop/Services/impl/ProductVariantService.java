package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.DTO.ProductVariantDTO;
import com.example.ShoesShop.Entity.Inventory;
import com.example.ShoesShop.Entity.ProductVariant;
import com.example.ShoesShop.Entity.VariantOptionMapping;
import com.example.ShoesShop.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private VariantOptionMappingRepository variantOptionMappingRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    public ProductVariant getVariantById(Long id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + id));
    }

    @Transactional
    public ProductVariant updateVariant(Long id, ProductVariantDTO variantDTO) {
        ProductVariant variant = getVariantById(id);

        // Cập nhật thông tin variant
        variant.setName(variantDTO.getName());
        variant.setPrice(variantDTO.getPrice());
        variant.setImg(variantDTO.getImg());

        // Nếu thay đổi SKU, kiểm tra SKU mới đã tồn tại chưa
        if (!variant.getSku().equals(variantDTO.getSku())) {
            if (productVariantRepository.findBySku(variantDTO.getSku()).isPresent()) {
                throw new IllegalArgumentException("SKU already exists: " + variantDTO.getSku());
            }
            variant.setSku(variantDTO.getSku());
        }

        // Cập nhật inventory
        Inventory inventory = variant.getInventory();
        if (inventory != null) {
            inventory.setQuantity(variantDTO.getQuantity());
            inventoryRepository.save(inventory);
        } else {
            // Tạo mới inventory nếu chưa có
            inventory = new Inventory();
            inventory.setQuantity(variantDTO.getQuantity());
            inventory.setVariant(variant);
            inventory.setStore(variant.getProduct().getStore());
            inventoryRepository.save(inventory);
            variant.setInventory(inventory);
        }

        // Cập nhật option mappings nếu có
        if (variantDTO.getOptionIds() != null && !variantDTO.getOptionIds().isEmpty()) {
            // Xóa mappings cũ
            List<VariantOptionMapping> oldMappings = variantOptionMappingRepository.findByVariantId(id);
            variantOptionMappingRepository.deleteAll(oldMappings);

            // Tạo mappings mới
            for (Long productOptionId : variantDTO.getOptionIds()) {
                VariantOptionMapping mapping = new VariantOptionMapping();
                mapping.setVariant(variant);
                mapping.setProductOption(productOptionRepository.findById(productOptionId)
                        .orElseThrow(() -> new RuntimeException("Product option not found with id: " + productOptionId)));
                variantOptionMappingRepository.save(mapping);
            }
        }

        return productVariantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(Long id) {
        ProductVariant variant = getVariantById(id);

        // Xóa các mappings với options
        List<VariantOptionMapping> mappings = variantOptionMappingRepository.findByVariantId(id);
        variantOptionMappingRepository.deleteAll(mappings);

        // Xóa inventory
        Inventory inventory = variant.getInventory();
        if (inventory != null) {
            inventoryRepository.delete(inventory);
        }

        // Xóa variant
        productVariantRepository.delete(variant);
    }

    @Transactional
    public void updateInventory(Long variantId, Integer quantity) {
        ProductVariant variant = getVariantById(variantId);
        Inventory inventory = variant.getInventory();

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setVariant(variant);
            inventory.setStore(variant.getProduct().getStore());
        }

        inventory.setQuantity(quantity);
        inventoryRepository.save(inventory);
    }
}