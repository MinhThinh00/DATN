package com.example.ShoesShop.Services.impl;


import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ShoesShop.DTO.DiscountDTO;
import com.example.ShoesShop.Entity.Discount;
import com.example.ShoesShop.Repository.DiscountRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountServiceImpl {


    @Autowired
    private EntityManager entityManager;

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

    public Page<DiscountDTO> searchDiscounts(String name,LocalDateTime startDate, LocalDateTime endDate, Boolean isActive, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Discount> query = cb.createQuery(Discount.class);
        Root<Discount> root = query.from(Discount.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            String searchPattern = "%" + name.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
            Predicate codePredicate = cb.like(cb.lower(root.get("code")), searchPattern);
            predicates.add(cb.or(namePredicate, codePredicate));
        }


        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }

        // Search by end date
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
        }

        if (isActive != null) {
            predicates.add(cb.equal(root.get("isActive"), isActive));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("id")));

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        List<Discount> result = entityManager.createQuery(query)
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .getResultList();


        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Discount> countRoot = countQuery.from(Discount.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        if (name != null && !name.trim().isEmpty()) {
            String searchPattern = "%" + name.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(countRoot.get("name")), searchPattern);
            Predicate codePredicate = cb.like(cb.lower(countRoot.get("code")), searchPattern);
            countPredicates.add(cb.or(namePredicate, codePredicate));
        }
        if (startDate != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("startDate"), startDate));
        }
        if (endDate != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("endDate"), endDate));
        }
        if (isActive != null) {
            countPredicates.add(cb.equal(countRoot.get("isActive"), isActive));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();


        Page<Discount> resultPage = new PageImpl<>(result, pageable, totalElements);
        return resultPage.map(this::convertToDTO);
    }

    public Page<DiscountDTO> getDiscount(Pageable pageable) {
        Page<Discount> discountPage = discountRepository.findAll( pageable);
        return discountPage.map(this::convertToDTO);
    }

    public DiscountDTO checkDiscount(String code) {
        Optional<Discount> discountOptional = discountRepository.findByCode(code);
        Discount discount = discountOptional.orElseThrow(() ->
                new IllegalArgumentException("Mã giảm giá không tồn tại: " + code));

        if (!discount.getIsActive()) {
            throw new IllegalArgumentException("Mã giảm giá đã bị vô hiệu hóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa bắt đầu.");
        }

        if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn.");
        }

        if (discount.getQuantity() != null && discount.getQuantity() <= 0) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
        }

        return convertToDTO(discount);
    }

}
