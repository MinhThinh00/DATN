package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    @Query("SELECT d FROM Discount d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) "+
            "AND (:startDate IS NULL OR d.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR d.endDate <= :endDate) " +
            "AND (:isActive IS NULL OR d.isActive = :isActive)")
    Page<Discount> findByFilter(
            @Param("name") String name,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    Boolean existsByCode(String code);
    Page<Discount> findAll( Pageable pageable);
}
