package com.example.ShoesShop.Services;

import com.example.ShoesShop.Entity.ProductGroup;

import java.util.List;
import java.util.Optional;

public interface ProductGroupService {
    List<ProductGroup> getAllProductGroups();
    Optional<ProductGroup> getProductGroupById(Long id);
    ProductGroup saveProductGroup(ProductGroup productGroup);
    void deleteProductGroup(Long id);
}