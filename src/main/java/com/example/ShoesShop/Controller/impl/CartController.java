package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.DTO.CartDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Services.impl.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long userId) {
        try {
            CartDTO cart = cartService.getCartByUserId(userId);
            ApiResponse response = new ApiResponse(true, "Cart retrieved successfully", cart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse> addToCart(
            @PathVariable Long userId,
            @RequestParam Long variantId,
            @RequestParam Integer quantity) {
        try {
            CartDTO updatedCart = cartService.addToCart(userId, variantId, quantity);
            ApiResponse response = new ApiResponse(true, "Item added to cart successfully", updatedCart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{userId}/update/{cartDetailId}")
    public ResponseEntity<ApiResponse> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartDetailId,
            @RequestParam Integer quantity) {
        try {
            CartDTO updatedCart = cartService.updateCartItem(userId, cartDetailId, quantity);
            ApiResponse response = new ApiResponse(true, "Cart item updated successfully", updatedCart);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{userId}/remove/{cartDetailId}")
    public ResponseEntity<ApiResponse> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartDetailId) {
        try {
            cartService.removeCartItem(userId, cartDetailId);
            ApiResponse response = new ApiResponse(true, "Cart item removed successfully", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            ApiResponse response = new ApiResponse(true, "Cart cleared successfully", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}