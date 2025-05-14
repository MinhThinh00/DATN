package com.example.ShoesShop.DTO;
import com.example.ShoesShop.Enum.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userName;
    private long storeId;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private OrderStatus status;
    private List<OrderDetailDTO> items;
    private PaymentDTO payment;
    private AddressDTO shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
