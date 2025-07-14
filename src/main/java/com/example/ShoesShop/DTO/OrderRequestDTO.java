package com.example.ShoesShop.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {
    @JsonProperty("isFromCart")
    private boolean isFromCart;
    private AddressDTO addressDTO;
    private String discount_code;
    private String note;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private Long storeId;
    public boolean isFromCart() {
        return isFromCart;
    }

    public void setFromCart(boolean fromCart) {
        this.isFromCart = fromCart;
    }
}

