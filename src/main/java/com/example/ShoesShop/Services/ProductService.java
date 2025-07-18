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

    Product createFullProduct(ProductInputDTO productDTO);

    Product updateProduct(Long productId, ProductInputDTO productDTO);

    void deleteProduct(Long productId);

    List<Product> getAllProducts();


    List<Product> getProductsByCategory(Long categoryId);

    Page<Product> getProductsByStore(Long storeId, Pageable pageable);


    List<Product> searchProductsByName(String name);


    ProductDTO getProductDetails(Long productId);

    List<Product> getProductsByGroupType(GroupType groupType);

    void addProductToGroup(Long productId, GroupType groupType);

    void removeProductFromGroup(Long productId, GroupType groupType);

    /**
     * Lấy danh sách loại nhóm của sản phẩm
     */
    List<GroupType> getGroupTypesByProductId(Long productId);

    Page<Product> getProductsByGroupTypePaginated(GroupType groupType, Pageable pageable);

    Page<Product> getProductSearch(Long storeId,GroupType groupType, String search, String type, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}