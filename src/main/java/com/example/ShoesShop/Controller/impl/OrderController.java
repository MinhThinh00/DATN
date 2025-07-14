package com.example.ShoesShop.Controller.impl;

import com.example.ShoesShop.DTO.OrderDTO;
import com.example.ShoesShop.DTO.OrderRequestDTO;
import com.example.ShoesShop.DTO.ProductDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Enum.OrderStatus;
import com.example.ShoesShop.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
//@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody OrderRequestDTO orderRequest, @RequestParam Long userId) {
        try {
            List<Order> orders = orderService.createOrder(orderRequest, userId);

            List<Map<String, Object>> responseDataList = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("orderId", order.getId());
                responseData.put("status", order.getStatus());
                responseData.put("totalAmount", order.getTotalPrice());
                responseData.put("storeId", order.getStore().getId());
                if ("VNPAY".equalsIgnoreCase(order.getPayment().getPaymentMethod())) {
                    responseData.put("vnpTxnRef", order.getPayment().getTransactionId());
                }
                responseDataList.add(responseData);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Orders created successfully", responseDataList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Failed to create order: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new ApiResponse(true, "Order retrieved successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<OrderDTO> orders = orderService.getOrdersByUserId(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("currentPage", orders.getNumber());
            response.put("totalItems", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    @GetMapping("/user/getall/{userId}")
    public ResponseEntity<ApiResponse> getAllByUserId(
            @PathVariable Long userId
    ){
        try {
            List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders);
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse> getOrdersByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<OrderDTO> orders = orderService.getOrdersByStoreId(storeId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("currentPage", orders.getNumber());
            response.put("totalItems", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderDTO> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid order status", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse> getOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            List<OrderDTO> orders = orderService.getOrdersByDateRange(start, end);
            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid date format. Use yyyy-MM-dd HH:mm:ss", null));
        }
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updatedOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid order status", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(new ApiResponse(true, "Order deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/store/{storeId}/filter")
    public ResponseEntity<ApiResponse> getOrderbyFilter(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt-desc") String sort
    ){
        try {
            String sortField = "orderDate";
            Sort.Direction direction = Sort.Direction.DESC;
            if (sort != null && !sort.trim().isEmpty()) {
                String[] sortParams = sort.split("-");
                if (sortParams.length > 0) {
                    sortField = sortParams[0];
                    if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
                        direction = Sort.Direction.DESC;
                    } else {
                        direction = Sort.Direction.ASC;
                    }
                }
            }
            if (page < 0) page = 0;
            if (size <= 0) size = 10;
            OrderStatus orderStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    orderStatus = OrderStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(false, "Invalid order status: " + status, null));
                }
            }

            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    return ResponseEntity.badRequest().body(
                            new ApiResponse(false, "Ngày bắt đầu phải trước ngày kết thúc", null));
                }
            }


            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<OrderDTO> orders = orderService.getOrdersByFilter(storeId, startDate, endDate, orderStatus, pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orders.getContent());
            response.put("currentPage", orders.getNumber());
            response.put("totalItems", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());

            return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}