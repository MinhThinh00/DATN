package com.example.ShoesShop.Services;

import com.example.ShoesShop.DTO.OrderDTO;
import com.example.ShoesShop.DTO.OrderRequestDTO;
import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Enum.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequestDTO orderRequest, Long userId);

    OrderDTO getOrderById(Long orderId);
    
    List<OrderDTO> getOrdersByUserId(Long userId);
    
    Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable);
    
    List<OrderDTO> getOrdersByStoreId(Long storeId);
    
    Page<OrderDTO> getOrdersByStoreId(Long storeId, Pageable pageable);
    
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    
    List<OrderDTO> getOrdersByDateRange(LocalDateTime start, LocalDateTime end);
    
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);
    
    void deleteOrder(Long orderId);

    Page<OrderDTO> getOrdersByFilter(Long storeId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus orderStatus, Pageable pageable);

}