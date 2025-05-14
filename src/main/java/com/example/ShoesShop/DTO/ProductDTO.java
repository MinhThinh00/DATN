package com.example.ShoesShop.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private CategoryDTO Category;
    private Long StoreId;
    private List<ProductImageDTO> images;
    private List<ProductGroupDTO> groups;
    private List<ProductOptionDTO> options;
    private List<ProductVariantDTO> variants;

}
