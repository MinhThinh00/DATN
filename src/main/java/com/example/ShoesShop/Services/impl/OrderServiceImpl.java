package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.Config.VnpayConfig;
import com.example.ShoesShop.DTO.*;
import com.example.ShoesShop.Entity.*;
import com.example.ShoesShop.Enum.OrderStatus;
import com.example.ShoesShop.Enum.PaymentMethod;
import com.example.ShoesShop.Enum.PaymentStatus;
import com.example.ShoesShop.Repository.*;
import com.example.ShoesShop.Services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VnpayConfig vnpayConfig;

    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    @Autowired
    private ProductVariantRepository productVariantRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CartService cartService;

    @Override
    @Transactional
    public Order createOrder(OrderRequestDTO orderRequest, Long userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        
        // Find the store
        Store store = storeRepository.findById(orderRequest.getStoreId())
                .orElseThrow(() -> new NoSuchElementException("Store not found with ID: " + orderRequest.getStoreId()));
        
        // Create or update address
        Address address = new Address();
        address.setUser(user);
        address.setPhone(orderRequest.getPhone());
        address.setDefault(false);
        address.setAddress(orderRequest.getAddress());
        address = addressRepository.save(address);
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStore(store); // Set the store directly from the request
        
        order.setTotalPrice(orderRequest.getTotalAmount());
        order.setTotalQuantity(orderRequest.getItems().stream()
                .mapToInt(OrderItemDTO::getQuantity)
                .sum());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setShippingAddress(address);
        
        // Save order to get ID
        order = orderRepository.save(order);
        
        // Create order details and check inventory
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderItemDTO item : orderRequest.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            
            ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new NoSuchElementException("Product variant not found with ID: " + item.getVariantId()));
            
            // Check if the product is available in the specified store
            Inventory inventory = inventoryRepository.findByVariantIdAndStoreId(variant.getId(), store.getId())
                    .orElseThrow(() -> new RuntimeException("Product " + variant.getProduct().getName() + 
                            " (" + variant.getName() + ") is not available in the selected store"));
            
            // Check if there's enough stock
            if (inventory.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for product " + variant.getProduct().getName() + 
                        " (" + variant.getName() + "). Available: " + inventory.getQuantity() + 
                        ", Requested: " + item.getQuantity());
            }
            
            // Update inventory
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
            
            detail.setProductVariant(variant);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getPrice());
            detail.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderDetails.add(detail);
        }
        
        order.setOrderDetails(orderDetails);
        
        // Create payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(orderRequest.getTotalAmount());
        
        if ("cod".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            payment.setPaymentMethod(String.valueOf(PaymentMethod.COD));
            payment.setStatus(PaymentStatus.PENDING);
        } else if ("vnpay".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            payment.setPaymentMethod(String.valueOf(PaymentMethod.VNPAY));
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(vnpayConfig.getRandomNumber(8));
        }
        
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        order.setPayment(payment);
        
        // Save the complete order
        order = orderRepository.save(order);
        
        // Only clear cart items if they were ordered from the cart
        if (orderRequest.isFromCart()) {
            List<Long> cartItemIds = orderRequest.getItems().stream()
                    .filter(item -> item.getId() != null) // Filter out items without cart IDs
                    .map(OrderItemDTO::getId)
                    .toList();
            
            if (!cartItemIds.isEmpty()) {
                for (Long cartItemId : cartItemIds) {
                    try {
                        cartService.removeCartItem(userId, cartItemId);
                    } catch (Exception e) {
                        // Log the error but continue with the order
                        System.err.println("Failed to remove cart item " + cartItemId + ": " + e.getMessage());
                    }
                }
            }
        }
        
        return order;
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::convertToDTO);
    }

    @Override
    public List<OrderDTO> getOrdersByStoreId(Long storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderDTO> getOrdersByStoreId(Long storeId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStoreId(storeId, pageable);
        return orders.map(this::convertToDTO);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        // If order is completed, update payment status
        if (status == OrderStatus.COMPLETED && order.getPayment() != null) {
            order.getPayment().setStatus(PaymentStatus.COMPLETED);
            order.getPayment().setUpdatedAt(LocalDateTime.now());
        }
        
        return this.convertToDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        orderRepository.findAndDeleteById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
    }
    
    // Helper method to convert Order to OrderDTO
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getFullName());
        dto.setStoreId(order.getStore().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setTotalQuantity(order.getTotalQuantity());
        dto.setStatus(OrderStatus.valueOf(order.getStatus().toString()));
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getShippingAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setId(order.getShippingAddress().getId());
            addressDTO.setAddress(order.getShippingAddress().getAddress());
            addressDTO.setPhone(order.getShippingAddress().getPhone());
            dto.setShippingAddress(addressDTO);
        }
        
        // Convert order details
        if (order.getOrderDetails() != null) {
            List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream()
                .map(detail -> {
                    OrderDetailDTO detailDTO = new OrderDetailDTO();
                    detailDTO.setId(detail.getId());
                    detailDTO.setVariant(convertPrductVariantToDTO(detail.getProductVariant()));
                    detailDTO.setQuantity(detail.getQuantity());
                    detailDTO.setUnitPrice(detail.getUnitPrice());
                    detailDTO.setTotalPrice(detail.getTotalPrice());
                    return detailDTO;
                })
                .collect(Collectors.toList());
            dto.setItems(detailDTOs);
        }
        
        // Convert payment
        if (order.getPayment() != null) {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setId(order.getPayment().getId());
            paymentDTO.setAmount(order.getPayment().getAmount());
            paymentDTO.setPaymentMethod(order.getPayment().getPaymentMethod());
            paymentDTO.setStatus(PaymentStatus.valueOf(order.getPayment().getStatus().toString()));
            paymentDTO.setCreatedAt(order.getPayment().getCreatedAt());
            paymentDTO.setUpdatedAt(order.getPayment().getUpdatedAt());
            dto.setPayment(paymentDTO);
        }
        
        return dto;
    }
    public ProductVariantDTO convertPrductVariantToDTO(ProductVariant variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setId(variant.getId());
        dto.setName(variant.getName());
        dto.setProductName(variant.getProduct().getName());
        dto.setImg(variant.getImg());
        dto.setQuantity(variant.getInventory().getQuantity());
        dto.setPrice(variant.getPrice());
        return dto;
    }
    public OrderDetailDTO convertToDTO(OrderDetail detail) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(detail.getId());
        dto.setVariant(convertPrductVariantToDTO(detail.getProductVariant()));
        dto.setQuantity(detail.getQuantity());
        dto.setUnitPrice(detail.getUnitPrice());
        dto.setTotalPrice(detail.getTotalPrice());
        return dto;
    }
}