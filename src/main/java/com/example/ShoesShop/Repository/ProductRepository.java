package com.example.ShoesShop.Repository;


import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Enum.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStoreId(Long storeId);

    Page<Product> findByStoreId(Long storeId, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT  p FROM Product p JOIN p.groupMappings gm JOIN gm.productGroup pg WHERE pg.type = :groupType")
    Page<Product> findByGroupType(@Param("groupType") GroupType groupType, Pageable pageable);



    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           // "AND (:Group IS NULL OR p.grouptype)"+
            "AND (:type IS NULL OR :type = '' OR LOWER(p.category.name) = LOWER(:type)) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> findProductsWithFilters(
            @Param("storeId") Long storeId,
            @Param("search") String search,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.groupMappings gm JOIN gm.productGroup pg " +
            "WHERE p.store.id = :storeId " +
            "AND (:groupType IS NULL OR pg.type = :groupType) " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:type IS NULL OR :type = '' OR LOWER(p.category.name) = LOWER(:type)) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Product> searchProductsFilte(
            @Param("storeId") Long storeId,
            @Param("groupType") GroupType groupType,
            @Param("search") String search,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

}