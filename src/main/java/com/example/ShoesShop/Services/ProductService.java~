package com.example.ShoesShop.Services;

import com.example.ShoesShop.DTO.Product.ProductInputDTO;
import com.example.ShoesShop.DTO.ProductDTO;
import com.example.ShoesShop.Entity.Product;
import com.example.ShoesShop.Enum.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    /**
     * Tạo sản phẩm mới với đầy đủ thông tin
     */
    Product createFullProduct(ProductInputDTO productDTO);

    /**
     * Cập nhật thông tin sản phẩm
     */
    Product updateProduct(Long productId, ProductInputDTO productDTO);

    /**
     * Xóa sản phẩm
     */
    void deleteProduct(Long productId);

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    List<Product> getAllProducts();

    /**
     * Lấy sản phẩm theo danh mục
     */
    List<Product> getProductsByCategory(Long categoryId);

    /**
     * Lấy sản phẩm theo cửa hàng
     */
    Page<Product> getProductsByStore(Long storeId, Pageable pageable);

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    List<Product> searchProductsByName(String name);

    /**
     * Lấy chi tiết sản phẩm
     */
    ProductDTO getProductDetails(Long productId);

    /**
     * Lấy sản phẩm theo loại nhóm
     */
    List<Product> getProductsByGroupType(GroupType groupType);

    /**
     * Thêm sản phẩm vào nhóm
     */
    void addProductToGroup(Long productId, GroupType groupType);

    /**
     * Xóa sản phẩm khỏi nhóm
     */
    void removeProductFromGroup(Long productId, GroupType groupType);

    /**
     * Lấy danh sách loại nhóm của sản phẩm
     */
    List<GroupType> getGroupTypesByProductId(Long productId);

    Page<Product> getProductsByGroupTypePaginated(GroupType groupType, Pageable pageable);

    Page<Product> getProductSearch(Long storeId,GroupType groupType, String search, String type, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}