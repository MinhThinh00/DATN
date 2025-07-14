package com.example.ShoesShop.Controller.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.example.ShoesShop.Entity.Payment;
import com.example.ShoesShop.Enum.OrderStatus;
import com.example.ShoesShop.Enum.PaymentStatus;
import com.example.ShoesShop.Repository.PaymentRepository;
import com.example.ShoesShop.Services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Services.impl.VNPAYService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private VNPAYService vnpayService;
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderService orderService;

    private static final String FRONTEND_URL = "http://localhost:5173/profile";

    @PostMapping("/create-payment")
    public ResponseEntity<ApiResponse> createPayment(
            HttpServletRequest request,
            @RequestParam int amount,
            @RequestParam(defaultValue = "Payment for order") String orderInfo,
            @RequestParam String vnpTxnRef

    ) {

        try {
            logger.info("Creating payment for amount: {}, orderInfo: {}", amount, orderInfo);
            List<Payment> payments = paymentRepository.findByTransactionId(vnpTxnRef);
            if (payments.isEmpty()) {
                logger.error("Invalid vnp_TxnRef: {} does not exist in database", vnpTxnRef);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Invalid vnp_TxnRef: Transaction ID does not exist", null));
            }
            // Get base URL from request
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            
            logger.info("Base URL for payment return: {}", baseUrl);
            
            // Create payment URL
            String paymentUrl = vnpayService.createOrder(request, amount, orderInfo, baseUrl, vnpTxnRef);
            
            Map<String, String> data = new HashMap<>();
            data.put("paymentUrl", paymentUrl);
            
            return ResponseEntity.ok(new ApiResponse(true, "Payment URL created successfully", data));
        } catch (Exception e) {
            logger.error("Failed to create payment URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to create payment URL: " + e.getMessage(), null));
        }
    }

    @GetMapping("/payment-callback")
    public RedirectView paymentCallback(HttpServletRequest request) {

        String redirectUrl = "";

        int paymentStatus = vnpayService.orderReturn(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("paymentStatus", paymentStatus);
        
        String message;
        boolean success;
        String paymentStatusStr;
        
        if (paymentStatus == 1) {
            message = "Payment successful";
            success = true;
            paymentStatusStr = "COMPLETED";

            // Get transaction details
            Map<String, String> transactionDetails = vnpayService.getTransactionDetails(request);
            response.putAll(transactionDetails);
            
            // Update order status in the database
            String vnp_TxnRef = transactionDetails.get("vnp_TxnRef");
            // TODO: Call your order service to update the status
            //orderService.updateOrderStatus(orderId, orderStatus);

            try {
                List<Payment> payments = paymentRepository.findByTransactionId(vnp_TxnRef);
//                Payment payment = paymentRepository.findByTransactionId(vnp_TxnRef)
//                        .orElseThrow(() -> new NoSuchElementException("Payment not found for transaction: " + vnp_TxnRef));
//                if (payment.getStatus() != PaymentStatus.PENDING) {
//                    logger.info("Payment already processed for transaction: {}", vnp_TxnRef);
//                    redirectUrl = FRONTEND_URL;
//                }
//                payment.setStatus(PaymentStatus.COMPLETED);
//                payment.setUpdatedAt(LocalDateTime.now());
//                orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.PROCESSING);
//                paymentRepository.save(payment);
//                response.put("paymentStatus", paymentStatusStr);
                for (Payment payment : payments) {
                    if (payment.getStatus() != PaymentStatus.PENDING) {
                        logger.info("Payment already processed for transaction: {}", vnp_TxnRef);
                        continue;
                    }
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setUpdatedAt(LocalDateTime.now());
                    orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.PROCESSING);
                    paymentRepository.save(payment);
                    response.put("paymentStatus", paymentStatusStr);
                }
                redirectUrl = FRONTEND_URL ;
            } catch (Exception e) {
                logger.error("Failed to update payment status for transaction: {}", vnp_TxnRef, e);
                redirectUrl = FRONTEND_URL + "&error=database_update_failed";
            }
            
            response.put("orderStatus", paymentStatusStr);
        } else if (paymentStatus == 0) {
            paymentStatusStr = "FAILED";

            // Get transaction details để lấy vnp_TxnRef
            Map<String, String> transactionDetails = vnpayService.getTransactionDetails(request);
            String vnp_TxnRef = transactionDetails.get("vnp_TxnRef");

            try {
//                Payment payment = paymentRepository.findByTransactionId(vnp_TxnRef)
//                        .orElseThrow(() -> new NoSuchElementException("Payment not found for transaction: " + vnp_TxnRef));
//
//                if (payment.getStatus() != PaymentStatus.PENDING) {
//                    logger.info("Payment already processed for transaction: {}", vnp_TxnRef);
//                } else {
//                    payment.setStatus(PaymentStatus.FAILED);
//                    payment.setUpdatedAt(LocalDateTime.now());
//                    paymentRepository.save(payment);
//
//                    orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.CANCELLED);
//                    logger.info("Payment failed for transaction: {}. Payment status set to: {}", vnp_TxnRef, paymentStatusStr);
//                }
//                redirectUrl = FRONTEND_URL;
                List<Payment> payments = paymentRepository.findByTransactionId(vnp_TxnRef);

                if (payments.isEmpty()) {
                    throw new NoSuchElementException("Payment not found for transaction: " + vnp_TxnRef);
                }

                for (Payment payment : payments) {
                    if (payment.getStatus() != PaymentStatus.PENDING) {
                        logger.info("Payment already processed for transaction: {}", vnp_TxnRef);
                        continue;
                    }
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    orderService.updateOrderStatus(payment.getOrder().getId(), OrderStatus.CANCELLED);
                }
                redirectUrl = FRONTEND_URL;
            } catch (Exception e) {
                logger.error("Failed to update payment status for transaction: {}", vnp_TxnRef, e);
                redirectUrl = FRONTEND_URL;
            }
        } else {
            message = "Invalid signature";
            success = false;
            paymentStatusStr = "INVALID";
            response.put("orderStatus", paymentStatusStr);
            logger.error("Invalid signature in payment callback. Order status set to: {}", paymentStatusStr);
        }
        
        // return ResponseEntity.ok(new ApiResponse(success, message, response));
        return new RedirectView(redirectUrl);
    }
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment service is up and running");
    }
}