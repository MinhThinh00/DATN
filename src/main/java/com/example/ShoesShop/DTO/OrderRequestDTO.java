package com.example.ShoesShop.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {
    private boolean isFromCart;
//    private String fullName;
//    private String email;
//    private String phone;
//    private String province;
//    private String district;
//    private String ward;
//    private String address;
    private AddressDTO addressDTO;
    private String discount_code;
    private String note;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private Long storeId;

}

