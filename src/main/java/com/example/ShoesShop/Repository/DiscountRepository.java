package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    @Query(value = "SELECT * FROM discount d WHERE " +
            "(:name IS NULL OR " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%'))) " +
            "AND (:startDate IS NULL OR d.start_date >= CAST(:startDate AS TIMESTAMP)) " +
            "AND (:endDate IS NULL OR d.end_date <= CAST(:endDate AS TIMESTAMP)) " +
            "AND (:isActive IS NULL OR d.is_active = CAST(:isActive AS BOOLEAN)) " +
            "ORDER BY d.id",
            countQuery = "SELECT COUNT(*) FROM discount d WHERE " +
                    "(:name IS NULL OR " +
                    "LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%')) OR " +
                    "LOWER(d.code) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%'))) " +
                    "AND (:startDate IS NULL OR d.start_date >= CAST(:startDate AS TIMESTAMP)) " +
                    "AND (:endDate IS NULL OR d.end_date <= CAST(:endDate AS TIMESTAMP)) " +
                    "AND (:isActive IS NULL OR d.is_active = CAST(:isActive AS BOOLEAN))",
            nativeQuery = true)
    Page<Discount> findByFilter(
            @Param("name") String name,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    Boolean existsByCode(String code);
    Page<Discount> findAll( Pageable pageable);

    Optional<Discount> findByCode(String code);
}
