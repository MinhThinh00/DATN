package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.DTO.Product.ProductInputDTO;
import com.example.ShoesShop.DTO.ProductDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Enum.GroupType;
import com.example.ShoesShop.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct( @RequestBody ProductInputDTO productDTO) {
        try {
            Product product = productService.createFullProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Product created successfully", product.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to create product: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductInputDTO productDTO) {
        try {
            Product product = productService.updateProduct(productId, productDTO);
            return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", product.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to update product: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to delete product: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> productService.getProductDetails(product.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO productDTO = productService.getProductDetails(productId);
        return ResponseEntity.ok(productDTO);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> productService.getProductDetails(product.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse> getProductsByStore(
            @PathVariable Long storeId,
            @PageableDefault(page = 0, size = 2, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable
            ) {
        Page<Product> products = productService.getProductsByStore(storeId, pageable);
        List<ProductDTO> productDTOs = products.getContent().stream()
                .map(product -> productService.getProductDetails(product.getId()))
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("products", productDTOs);
        response.put("currentPage", products.getNumber());
        response.put("totalItems", products.getTotalElements());
        response.put("totalPages", products.getTotalPages());
        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        List<Product> products = productService.searchProductsByName(query);
        List<ProductDTO> productDTOs = products.stream()
                .map(product -> productService.getProductDetails(product.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/group/{groupType}")
    public ResponseEntity<ApiResponse> getProductsByGroup(
            @PathVariable String groupType,
            @PageableDefault(page = 0, size = 16, sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        try {
            GroupType type = GroupType.valueOf(groupType.toUpperCase());
            Page<Product> productPage = productService.getProductsByGroupTypePaginated(type, pageable);

            List<ProductDTO> productDTOs = productPage.getContent().stream()
                    .map(product -> productService.getProductDetails(product.getId()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("products", productDTOs);
            response.put("currentPage", productPage.getNumber());
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());

            return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid group type: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{productId}/group/{groupType}")
    public ResponseEntity<ApiResponse> addProductToGroup(
            @PathVariable Long productId,
            @PathVariable String groupType) {
        try {
            GroupType type = GroupType.valueOf(groupType.toUpperCase());
            productService.addProductToGroup(productId, type);
            return ResponseEntity.ok(new ApiResponse(true, "Product added to group successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to add product to group: " + e.getMessage(), null));
        }
    }
}