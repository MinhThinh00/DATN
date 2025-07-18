package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.DTO.DiscountDTO;
import com.example.ShoesShop.DTO.ProductDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Entity.Discount;
import com.example.ShoesShop.Services.impl.DiscountServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {
    private final DiscountServiceImpl discountService;

    public DiscountController(DiscountServiceImpl discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    public ResponseEntity<?> createDiscount(@RequestBody DiscountDTO discount) {
        try {
            DiscountDTO createdDiscount = discountService.createDiscount(discount);
            return ResponseEntity.status(201).body(createdDiscount);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchDiscounts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(page = 0, size = 10, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
    {
        Page<DiscountDTO> discounts = discountService.searchDiscounts(name, startDate, endDate, isActive, pageable);
        List<DiscountDTO> discountList = discounts.getContent().stream().collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("discount", discountList);
        response.put("currentPage", discounts.getNumber());
        response.put("totalItems", discounts.getTotalElements());
        response.put("totalPages", discounts.getTotalPages());
        return ResponseEntity.ok(new ApiResponse(true, "Discounts retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountDTO> getById(@PathVariable Long id) {
        try {
            DiscountDTO discount = discountService.getDiscountById(id);
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountDTO> updateDiscount(@PathVariable Long id, @RequestBody DiscountDTO discount) {
        try {
            DiscountDTO updatedDiscount = discountService.updateDiscount(id, discount);
            return ResponseEntity.ok(updatedDiscount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        ApiResponse apiResponse = new ApiResponse(true,"xóa thanh công ", null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllDiscounts(
            @RequestParam(defaultValue = "0") int page,
            @PageableDefault(page = 0, size = 10, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable)
    {
        Page<DiscountDTO> discounts = discountService.getDiscount(pageable);
        List<DiscountDTO> discountList = discounts.getContent().stream().collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("discount", discountList);
        response.put("currentPage", discounts.getNumber());
        response.put("totalItems", discounts.getTotalElements());
        response.put("totalPages", discounts.getTotalPages());
        return ResponseEntity.ok(new ApiResponse(true, "Discounts retrieved successfully", response));
    }

    @GetMapping("check-discount")
    public ResponseEntity<ApiResponse> checkDiscount(@RequestParam String code) {
        try {
            DiscountDTO discount = discountService.checkDiscount(code);
            ApiResponse response = new ApiResponse(true, "Mã giảm giá hợp lệ", discount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}