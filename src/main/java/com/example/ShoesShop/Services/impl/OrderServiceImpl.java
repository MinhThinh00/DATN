package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.Config.VnpayConfig;
import com.example.ShoesShop.DTO.*;
import com.example.ShoesShop.Entity.*;
import com.example.ShoesShop.Enum.OrderStatus;
import com.example.ShoesShop.Enum.PaymentMethod;
import com.example.ShoesShop.Enum.PaymentStatus;
import com.example.ShoesShop.Repository.*;
import com.example.ShoesShop.Services.OrderService;
import com.example.ShoesShop.Services.SendEmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SendEmailService sendEmailService;

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

    @Autowired
    private AddressServiceImpl addressService;
    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public List<Order> createOrder(OrderRequestDTO orderRequest, Long userId) {
        try {
            String tran= vnpayConfig.getRandomNumber(8);
            // Validate input
            if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
                throw new IllegalArgumentException("Order items cannot be null or empty");
            }

            // Find the user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

            // Handle address
            Address address;
            AddressDTO addressDTO = orderRequest.getAddressDTO();
            if (addressDTO.getId() != null) {
                address = addressRepository.findById(addressDTO.getId())
                        .orElseThrow(() -> new NoSuchElementException("Address not found with ID: " + addressDTO.getId()));
            } else {
                address = new Address();
                address.setUser(user);
                address.setPhone(addressDTO.getPhone());
                address.setProvince(addressDTO.getProvince());
                address.setDistrict(addressDTO.getDistrict());
                address.setWard(addressDTO.getWard());
                address.setAddress(addressDTO.getAddress());
                address.setDefault(addressDTO.isDefault());
                address = addressRepository.save(address);
            }

            // Handle discount
            Discount discount = Optional.ofNullable(orderRequest.getDiscount_code())
                    .map(code -> discountRepository.findByCode(code)
                            .orElseThrow(() -> new RuntimeException("Discount code not found")))
                    .orElse(null);

            // Group items by store based on product's store
            Map<Long, List<OrderItemDTO>> itemsByStore = new HashMap<>();

            for (OrderItemDTO item : orderRequest.getItems()) {
                if (item.getVariantId() == null) {
                    throw new IllegalArgumentException("Variant ID cannot be null for item");
                }

                ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                        .orElseThrow(() -> new NoSuchElementException("Product variant not found with ID: " + item.getVariantId()));

                Product product = variant.getProduct();
                if (product == null || product.getStore() == null) {
                    throw new IllegalArgumentException("Product or store not found for variant ID: " + item.getVariantId());
                }

                Long storeId = product.getStore().getId();
                itemsByStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(item);
            }

            // Apply discount to first order only or distribute proportionally
            if (discount != null) {
                discount.setQuantity(discount.getQuantity() - 1);
            }

            List<Order> orders = new ArrayList<>();

            // Create an order for each store
            boolean isFirstOrder = true;
            for (Map.Entry<Long, List<OrderItemDTO>> entry : itemsByStore.entrySet()) {
                Long storeId = entry.getKey();
                List<OrderItemDTO> storeItems = entry.getValue();

                // Find the store
                Store store = storeRepository.findById(storeId)
                        .orElseThrow(() -> new NoSuchElementException("Store not found with ID: " + storeId));

                // Calculate totals for this store's order
                BigDecimal storeTotalPrice = storeItems.stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int storeTotalQuantity = storeItems.stream()
                        .mapToInt(OrderItemDTO::getQuantity)
                        .sum();

                // Create order for this store
                Order order = new Order();
                order.setUser(user);
                order.setStore(store);
                // Apply discount only to the first order (you can modify this logic as needed)
                order.setDiscount(isFirstOrder ? discount : null);
                order.setTotalPrice(storeTotalPrice);
                order.setTotalQuantity(storeTotalQuantity);
                order.setStatus(OrderStatus.PENDING);
                order.setCreatedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                order.setShippingAddress(address);

                // Save order to get ID
                order = orderRepository.save(order);

                // Create order details and check inventory for this store
                List<OrderDetail> orderDetails = new ArrayList<>();
                for (OrderItemDTO item : storeItems) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(order);

                    ProductVariant variant = productVariantRepository.findById(item.getVariantId())
                            .orElseThrow(() -> new NoSuchElementException("Product variant not found with ID: " + item.getVariantId()));

                    // Check if the product is available in the specified store
                    Inventory inventory = inventoryRepository.findByVariantIdAndStoreId(variant.getId(), store.getId())
                            .orElseThrow(() -> new RuntimeException("Product " + variant.getProduct().getName() +
                                    " (" + variant.getName() + ") is not available in store " + store.getName()));

                    // Check if there's enough stock
                    if (inventory.getQuantity() < item.getQuantity()) {
                        throw new RuntimeException("Not enough stock for product " + variant.getProduct().getName() +
                                " (" + variant.getName() + ") in store " + store.getName() +
                                ". Available: " + inventory.getQuantity() + ", Requested: " + item.getQuantity());
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

                // Create payment for this order
                Payment payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(order.getTotalPrice());
                payment.setCreatedAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());

                if ("cod".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                    payment.setPaymentMethod(String.valueOf(PaymentMethod.COD));
                    payment.setStatus(PaymentStatus.PENDING);
                } else if ("vnpay".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
                    payment.setPaymentMethod(String.valueOf(PaymentMethod.VNPAY));
                    payment.setStatus(PaymentStatus.PENDING);
                    payment.setTransactionId(tran);
                    // Only set to PROCESSING for VNPay
                    //order.setStatus(OrderStatus.PROCESSING);
                }

                order.setPayment(payment);

                // Save the complete order
                order = orderRepository.save(order);
                orders.add(order);

                // Send order confirmation email
                sendEmailService.sendOrderConfirmationEmail(order);

                isFirstOrder = false;
            }

            // Clear cart items if ordered from cart
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

            return orders;
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
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
    public List<OrderDTO> getAllOrderByUserId( Long userId) {
        List<Order> orders = orderRepository.findAllByUserId( userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

        this.validateOrderStatusTransition(order.getStatus(), status);

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

    @Override
    public Page<OrderDTO> getOrdersByFilter(Long storeId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus orderStatus, Pageable pageable) {
//        Page<Order> orderPage = orderRepository.findByFilter(storeId, startDate, endDate, orderStatus, pageable);
//        return orderPage.map(this::convertToDTO);

        StringBuilder sql = new StringBuilder("SELECT * FROM orders o WHERE o.store_id = :storeId");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM orders o WHERE o.store_id = :storeId");
        Map<String, Object> params = new HashMap<>();
        params.put("storeId", storeId);

        // Thêm điều kiện startDate nếu không null
        if (startDate != null) {
            sql.append(" AND o.created_at >= :startDate");
            countSql.append(" AND o.created_at >= :startDate");
            params.put("startDate", startDate);
        }

        // Thêm điều kiện endDate nếu không null
        if (endDate != null) {
            sql.append(" AND o.created_at <= :endDate");
            countSql.append(" AND o.created_at <= :endDate");
            params.put("endDate", endDate);
        }

        // Thêm điều kiện status nếu không null
        if (orderStatus != null) {
            sql.append(" AND o.status = :status");
            countSql.append(" AND o.status = :status");
            params.put("status", orderStatus.name());
        }

        // Thêm ORDER BY
        sql.append(" ORDER BY o.created_at DESC");

        // Tạo query
        Query query = entityManager.createNativeQuery(sql.toString(), Order.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());

        // Gán tham số
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
            countQuery.setParameter(param.getKey(), param.getValue());
        }

        // Phân trang
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Lấy danh sách kết quả
        @SuppressWarnings("unchecked")
        List<OrderDTO> orders = (List<OrderDTO>) query.getResultList().stream().map((Function<Order, OrderDTO>)  this::convertToDTO).collect(Collectors.toList());

        // Lấy tổng số bản ghi
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        // Trả về Page
        return new PageImpl<>(orders, pageable, total);
    }

    // Helper method to validate order status transitions
    private void validateOrderStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.PROCESSING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PROCESSING to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.COMPLETED) {
                    throw new IllegalStateException("Invalid status transition from SHIPPED to " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new IllegalStateException("Cannot change status from " + currentStatus);
            default:
                throw new IllegalStateException("Invalid current status: " + currentStatus);
        }
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
            addressDTO.setProvince(order.getShippingAddress().getProvince());
            addressDTO.setDistrict(order.getShippingAddress().getDistrict());
            addressDTO.setWard(order.getShippingAddress().getWard());
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