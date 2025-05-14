package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.Entity.ProductGroup;
import com.example.ShoesShop.Services.ProductGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-groups")
public class ProductGroupController {

    private final ProductGroupService productGroupService;

    @Autowired
    public ProductGroupController(ProductGroupService productGroupService) {
        this.productGroupService = productGroupService;
    }

    @GetMapping
    public ResponseEntity<List<ProductGroup>> getAllProductGroups() {
        List<ProductGroup> productGroups = productGroupService.getAllProductGroups();
        return new ResponseEntity<>(productGroups, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductGroup> getProductGroupById(@PathVariable Long id) {
        Optional<ProductGroup> productGroup = productGroupService.getProductGroupById(id);
        return productGroup.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ProductGroup> createProductGroup(@RequestBody ProductGroup productGroup) {
        ProductGroup savedProductGroup = productGroupService.saveProductGroup(productGroup);
        return new ResponseEntity<>(savedProductGroup, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductGroup> updateProductGroup(@PathVariable Long id, @RequestBody ProductGroup productGroup) {
        Optional<ProductGroup> existingProductGroup = productGroupService.getProductGroupById(id);
        if (existingProductGroup.isPresent()) {
            productGroup.setId(id);
            ProductGroup updatedProductGroup = productGroupService.saveProductGroup(productGroup);
            return new ResponseEntity<>(updatedProductGroup, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductGroup(@PathVariable Long id) {
        Optional<ProductGroup> existingProductGroup = productGroupService.getProductGroupById(id);
        if (existingProductGroup.isPresent()) {
            productGroupService.deleteProductGroup(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}