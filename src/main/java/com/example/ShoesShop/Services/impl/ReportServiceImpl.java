package com.example.ShoesShop.Services.impl;


import com.example.ShoesShop.DTO.Report.*;
import com.example.ShoesShop.Entity.*;
import com.example.ShoesShop.Enum.OrderStatus;
import com.example.ShoesShop.Repository.OrderDetailRepository;
import com.example.ShoesShop.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import com.example.ShoesShop.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;


    public Map<String, List<MonthlyRevenueDTO>> getRevenueByYear(int year) {
        // Lấy danh sách đơn hàng trong năm
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfYear, endOfYear);

        // Lấy danh sách cửa hàng
        Set<Store> stores = orders.stream()
                .map(Order::getStore)
                .collect(Collectors.toSet());

        // Nhóm đơn hàng theo tháng và cửa hàng
        Map<Integer, Map<Store, BigDecimal>> revenueByMonthAndStore = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().getMonthValue(),
                        Collectors.groupingBy(
                                Order::getStore,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Order::getTotalPrice,
                                        BigDecimal::add
                                )
                        )
                ));

        // Tạo dữ liệu trả về
        List<MonthlyRevenueDTO> data = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlyRevenueDTO monthlyRevenue = new MonthlyRevenueDTO();
            monthlyRevenue.setName("Tháng " + month);

            Map<String, BigDecimal> storeRevenues = new HashMap<>();
            for (Store store : stores) {
                BigDecimal revenue = revenueByMonthAndStore
                        .getOrDefault(month, Collections.emptyMap())
                        .getOrDefault(store, BigDecimal.ZERO);
                storeRevenues.put(store.getName(), revenue);
            }
            monthlyRevenue.setStoreRevenues(storeRevenues);
            data.add(monthlyRevenue);
        }

        return Collections.singletonMap("data", data);
    }
    public Map<String, List<Map<String, List<CategoryRevenueDTO>>>> getRevenueByCategory(int month, int year) {
        // Validate month
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        // Lấy danh sách đơn hàng trong tháng/năm
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

        // Nhóm dữ liệu theo cửa hàng và danh mục
        Map<Store, Map<String, BigDecimal>> revenueByStoreAndCategory = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.groupingBy(
                        orderDetail -> orderDetail.getOrder().getStore(),
                        Collectors.groupingBy(
                                orderDetail -> orderDetail.getProductVariant().getProduct().getCategory().getName(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        OrderDetail::getTotalPrice,
                                        BigDecimal::add
                                )
                        )
                ));

        // Tạo dữ liệu trả về
        List<Map<String, List<CategoryRevenueDTO>>> data = new ArrayList<>();
        for (Store store : revenueByStoreAndCategory.keySet()) {
            Map<String, List<CategoryRevenueDTO>> storeData = new HashMap<>();
            List<CategoryRevenueDTO> categoryRevenues = revenueByStoreAndCategory.get(store)
                    .entrySet().stream()
                    .map(entry -> new CategoryRevenueDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            storeData.put(store.getName(), categoryRevenues);
            data.add(storeData);
        }

        return Collections.singletonMap("data", data);
    }

//    // Báo cáo top 10 sản phẩm bán chạy (nhóm theo Product, cộng dồn các biến thể)
//    public Map<String, List<  TopProductDTO>> getTopProducts(int month, int year) {
//        if (month < 1 || month > 12) {
//            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
//        }
//
//        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
//        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
//        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
//
//        // Nhóm theo Product, tính tổng quantity và revenue từ tất cả các biến thể
//        Map<Product, ProductStats> productStatsMap = orders.stream()
//                .flatMap(order -> order.getOrderDetails().stream())
//                .collect(Collectors.groupingBy(
//                        orderDetail -> orderDetail.getProductVariant().getProduct(),
//                        Collectors.teeing(
//                                Collectors.summingInt(OrderDetail::getQuantity),
//                                Collectors.reducing(BigDecimal.ZERO, OrderDetail::getTotalPrice, BigDecimal::add),
//                                (quantity, revenue) -> new ProductStats(quantity, revenue)
//                        )
//                ));
//
//        // Chuyển thành danh sách DTO và sắp xếp giảm dần theo totalQuantitySold
//        List<  TopProductDTO> topProducts = productStatsMap.entrySet().stream()
//                .map(entry -> {
//                    Product product = entry.getKey();
//                    ProductStats stats = entry.getValue();
//                    return new   TopProductDTO(
//                            product.getId(),
//                            product.getName(),
//                            stats.quantity,
//                            stats.revenue
//                    );
//                })
//                .sorted(Comparator.comparingInt(  TopProductDTO::getTotalQuantitySold).reversed())
//                .limit(10)
//                .collect(Collectors.toList());
//
//        return Collections.singletonMap("data", topProducts);
//    }

    // Báo cáo top 10 sản phẩm bán chạy theo store
    public Map<String, List<Map<String, List< TopProductDTO>>>> getTopProducts(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        List<Order> orders = orderRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);

        // Nhóm theo Store, sau đó theo Product, tính tổng quantity và revenue
        Map<Store, Map<Product, ProductStats>> storeProductStatsMap = orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.groupingBy(
                        orderDetail -> orderDetail.getOrder().getStore(),
                        Collectors.groupingBy(
                                orderDetail -> orderDetail.getProductVariant().getProduct(),
                                Collectors.teeing(
                                        Collectors.summingInt(OrderDetail::getQuantity),
                                        Collectors.reducing(BigDecimal.ZERO, OrderDetail::getTotalPrice, BigDecimal::add),
                                        (quantity, revenue) -> new ProductStats(quantity, revenue)
                                )
                        )
                ));

        // Tạo dữ liệu trả về
        List<Map<String, List< TopProductDTO>>> data = new ArrayList<>();
        for (Store store : storeProductStatsMap.keySet()) {
            // Lấy top 10 sản phẩm cho cửa hàng
            List< TopProductDTO> topProducts = storeProductStatsMap.get(store).entrySet().stream()
                    .map(entry -> {
                        Product product = entry.getKey();
                        ProductStats stats = entry.getValue();
                        String imgUrl = product.getImages() != null && !product.getImages().isEmpty()
                                ? product.getImages().get(0).getImageURL()
                                : null;
                        return new  TopProductDTO(
                                product.getId(),
                                product.getName(),
                                imgUrl,
                                stats.quantity,
                                stats.revenue
                        );
                    })
                    .sorted(Comparator.comparingInt( TopProductDTO::getTotalQuantitySold).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            // Tạo object cho cửa hàng
            Map<String, List< TopProductDTO>> storeData = new HashMap<>();
            storeData.put("store" + store.getId(), topProducts);
            data.add(storeData);
        }

        return Collections.singletonMap("data", data);
    }

    // Lớp phụ để lưu trữ số liệu sản phẩm
    private static class ProductStats {
        int quantity;
        BigDecimal revenue;

        ProductStats(int quantity, BigDecimal revenue) {
            this.quantity = quantity;
            this.revenue = revenue;
        }
    }
    public StoreSummaryDTO getStoreSummary() {
        Integer year = 2025; // Hardcoded year
        List<Long> storeIdList = Arrays.asList(1L, 2L); // Hardcoded store IDs

        // Fetch orders
        List<Order> orders = orderRepository.findByCreatedAtYearAndStoreIds(year, storeIdList);

        // Calculate total orders
        long totalOrders = orders.size();

        // Calculate total revenue
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fetch order details for total products sold
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderCreatedAtYearAndStoreIds(year, storeIdList);
        int totalProductsSold = orderDetails.stream()
                .mapToInt(OrderDetail::getQuantity)
                .sum();

        // Calculate total unique customers
        long totalCustomers = orders.stream()
                .map(Order::getUser)
                .map(User::getId)
                .distinct()
                .count();

        return new StoreSummaryDTO(year, null, storeIdList, totalOrders, totalRevenue, totalProductsSold, totalCustomers);
    }
    public OrderStatusReportDTO getOrderStatusReport(String storeIds) {
        List<Long> storeIdList = Arrays.stream(storeIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Fetch orders for specified stores
        List<Order> orders = orderRepository.findByStoreIds(storeIdList);

        // Calculate counts by status
        long totalOrders = orders.size();
        long pendingConfirmation = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();
        long handedOverToShipper = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.SHIPPED)
                .count();
        long completed = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();

        return new OrderStatusReportDTO(storeIdList, totalOrders, pendingConfirmation, handedOverToShipper, completed);
    }
}
