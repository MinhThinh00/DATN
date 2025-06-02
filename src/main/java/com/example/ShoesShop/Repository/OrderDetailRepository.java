package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @Query("SELECT od FROM OrderDetail od WHERE YEAR(od.order.createdAt) = :year")
    List<OrderDetail> findByOrderCreatedAtYear(@Param("year") Integer year);

    @Query("SELECT od FROM OrderDetail od WHERE YEAR(od.order.createdAt) = :year AND MONTH(od.order.createdAt) = :month")
    List<OrderDetail> findByOrderCreatedAtYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    // New queries for store-specific order details
    @Query("SELECT od FROM OrderDetail od WHERE YEAR(od.order.createdAt) = :year AND od.order.store.id IN :storeIds")
    List<OrderDetail> findByOrderCreatedAtYearAndStoreIds(@Param("year") Integer year, @Param("storeIds") List<Long> storeIds);

    @Query("SELECT od FROM OrderDetail od WHERE YEAR(od.order.createdAt) = :year AND MONTH(od.order.createdAt) = :month AND od.order.store.id IN :storeIds")
    List<OrderDetail> findByOrderCreatedAtYearAndMonthAndStoreIds(@Param("year") Integer year, @Param("month") Integer month, @Param("storeIds") List<Long> storeIds);
}
