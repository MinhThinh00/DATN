package com.example.ShoesShop.Services.impl;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ShoesShop.DTO.DiscountDTO;
import com.example.ShoesShop.Entity.Discount;
import com.example.ShoesShop.Repository.DiscountRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class DiscountServiceImpl {
    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }
    public void deleteDiscount(Long id) {
        Discount existingDiscount = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with id: " + id));
        discountRepository.delete(existingDiscount);
    }
    public DiscountDTO getDiscountById(Long id) {
        Discount existingDiscount = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with id: " + id));
        return convertToDTO(existingDiscount);
    }
    public DiscountDTO createDiscount(DiscountDTO discountDTO) {

            if (discountDTO.getName() == null || discountDTO.getCode() == null) {
                throw new IllegalArgumentException("Name and code cannot be null");
            }
            if (discountDTO.getDiscountPercentage() <= 0) {
                throw new IllegalArgumentException("Discount percentage must be greater than 0");
            }
            if (discountDTO.getStartDate() == null || discountDTO.getEndDate() == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            // Check if start date is before end date
            if (discountDTO.getStartDate().compareTo(discountDTO.getEndDate()) > 0) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            if (discountRepository.existsByCode(discountDTO.getCode())) {
                throw new IllegalArgumentException("Mã code đã tồn tại");
            }
            Discount discount = new Discount();
            discount.setName(discountDTO.getName());
            discount.setCode(discountDTO.getCode());
            discount.setQuantity(discountDTO.getQuantity());
            discount.setDiscountPercentage(discountDTO.getDiscountPercentage());
            discount.setStartDate(discountDTO.getStartDate());
            discount.setEndDate(discountDTO.getEndDate());
            discount.setIsActive(discountDTO.isActive());
            
            Discount savedDiscount = discountRepository.save(discount);
            
            return convertToDTO(savedDiscount);

    }

    public DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO) {
        try {
            Discount existingDiscount = discountRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Discount not found with id: " + id));
            if (discountDTO.getName() == null || discountDTO.getCode() == null) {
                throw new IllegalArgumentException("Name and code cannot be null");
            }
            if (discountDTO.getDiscountPercentage() <= 0) {
                throw new IllegalArgumentException("Discount percentage must be greater than 0");
            }
            if (discountDTO.getStartDate() == null || discountDTO.getEndDate() == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            // Check if start date is before end date
            if (discountDTO.getStartDate().compareTo(discountDTO.getEndDate()) > 0) {
                throw new IllegalArgumentException("Start date must be before end date");
            }
            
            // Update the existing discount
            existingDiscount.setName(discountDTO.getName());
            existingDiscount.setCode(discountDTO.getCode());
            existingDiscount.setQuantity(discountDTO.getQuantity());
            existingDiscount.setDiscountPercentage(discountDTO.getDiscountPercentage());
            existingDiscount.setStartDate(discountDTO.getStartDate());
            existingDiscount.setEndDate(discountDTO.getEndDate());
            existingDiscount.setIsActive(discountDTO.isActive());
            
            // Save the updated discount
            Discount updatedDiscount = discountRepository.save(existingDiscount);
            
            return convertToDTO(updatedDiscount);
        } catch (IllegalArgumentException e) {
            // Handle validation error
            System.out.println("Validation error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to convert Discount entity to DiscountDTO
     * @param discount The discount entity to convert
     * @return The converted DiscountDTO
     */
    private DiscountDTO convertToDTO(Discount discount) {
        if (discount == null) {
            return null;
        }
        
        DiscountDTO discountDTO = new DiscountDTO();
        discountDTO.setId(discount.getId());
        discountDTO.setName(discount.getName());
        discountDTO.setCode(discount.getCode());
        discountDTO.setQuantity(discount.getQuantity());
        discountDTO.setDiscountPercentage(discount.getDiscountPercentage());
        discountDTO.setStartDate(discount.getStartDate());
        discountDTO.setEndDate(discount.getEndDate());
        discountDTO.setActive(discount.getIsActive());
        
        return discountDTO;
    }

    /**
     * Search for discounts based on start date, end date, and active status
     * @param startDate Optional start date to search from
     * @param endDate Optional end date to search to
     * @param isActive Optional active status filter
     * @return List of matching discount DTOs
     */
    public Page<DiscountDTO> searchDiscounts(String name,LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, Pageable pageable) {
        Page<Discount> discountPage = discountRepository.findByFilter(name,startDate, endDate, isActive, pageable);
        return discountPage.map(this::convertToDTO);
    }

    public Page<DiscountDTO> getDiscount(Pageable pageable) {
        Page<Discount> discountPage = discountRepository.findAll( pageable);
        return discountPage.map(this::convertToDTO);
    }
}
