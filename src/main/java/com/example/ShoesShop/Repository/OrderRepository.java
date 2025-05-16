package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Enum.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStoreId(Long storeId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStoreId(Long storeId);

    Optional<Order> findAndDeleteById(Long orderId);

    @Query("SELECT o FROM Order o WHERE o.store.id = :storeId " +
            "AND o.createdAt >= :startDate " +
            "AND o.createdAt <= :endDate " +
            "AND o.status = :status")
    Page<Order> findByFilter(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status,
            Pageable pageable);
}
