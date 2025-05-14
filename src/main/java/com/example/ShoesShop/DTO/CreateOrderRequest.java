package com.example.ShoesShop.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Long userId;
    private Long storeId;
    private Long addressId;
    private String paymentMethod;
    private boolean fromCart;  // Nếu true, tạo đơn hàng từ giỏ hàng, nếu false, tạo từ danh sách items bên dưới
    private List<OrderItemRequest> items; // Chỉ sử dụng khi fromCart = false (nhân viên tạo đơn)
}