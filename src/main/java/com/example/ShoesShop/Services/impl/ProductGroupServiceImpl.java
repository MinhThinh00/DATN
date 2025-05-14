package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.Entity.ProductGroup;
import com.example.ShoesShop.Repository.ProductGroupRepository;
import com.example.ShoesShop.Services.ProductGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductGroupServiceImpl implements ProductGroupService {

    private final ProductGroupRepository productGroupRepository;

    @Autowired
    public ProductGroupServiceImpl(ProductGroupRepository productGroupRepository) {
        this.productGroupRepository = productGroupRepository;
    }

    @Override
    public List<ProductGroup> getAllProductGroups() {
        return productGroupRepository.findAll();
    }

    @Override
    public Optional<ProductGroup> getProductGroupById(Long id) {
        return productGroupRepository.findById(id);
    }

    @Override
    public ProductGroup saveProductGroup(ProductGroup productGroup) {
        return productGroupRepository.save(productGroup);
    }

    @Override
    public void deleteProductGroup(Long id) {
        productGroupRepository.deleteById(id);
    }
}