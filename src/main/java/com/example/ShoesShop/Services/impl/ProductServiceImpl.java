package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.DTO.*;
import com.example.ShoesShop.DTO.Product.OptionInputDTO;
import com.example.ShoesShop.DTO.Product.ProductInputDTO;
import com.example.ShoesShop.DTO.Product.SizeDTO;
import com.example.ShoesShop.DTO.Product.VariantInputDTO;
import com.example.ShoesShop.Entity.*;
import com.example.ShoesShop.Enum.GroupType;
import com.example.ShoesShop.Repository.*;
import com.example.ShoesShop.exception.ResourceNotFoundException;
import com.example.ShoesShop.Services.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductDiscountRepository productDiscountRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private VariantOptionMappingRepository variantOptionMappingRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductGroupRepository productGroupRepository;
    @Autowired
    private ProductGroupMappingRepository productGroupMappingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Product createFullProduct(ProductInputDTO productDTO) {
        Product product = initializeProduct(productDTO);
        product = productRepository.save(product);

        saveProductImages(product, productDTO.getImages());
        saveProductGroups(product, productDTO.getProductGroupIds());
        saveProductOptions(product, productDTO.getOptions(), productDTO.getVariants());
        saveProductVariants(product, productDTO.getVariants());

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long productId, ProductInputDTO productDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        clearExistingData(product);
        updateProductDetails(product, productDTO);

        saveProductImages(product, productDTO.getImages());
        saveProductGroups(product, productDTO.getProductGroupIds());
        saveProductOptions(product, productDTO.getOptions(), productDTO.getVariants());
        saveProductVariants(product, productDTO.getVariants());

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
//
//        // Lưu product sau khi đã xóa các tham chiếu
//        productRepository.save(product);
//        clearExistingData(product);
//        productRepository.delete(product);
        ////-----------////
        try {
            // Bước 1: Tìm và nạp sản phẩm với tất cả các mối quan hệ
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            // Bước 2: Xóa các mối quan hệ từ con lên cha

            // Xóa ProductGroupMapping
            List<ProductGroupMapping> groupMappings = new ArrayList<>(product.getGroupMappings());
            for (ProductGroupMapping mapping : groupMappings) {
                mapping.setProduct(null);
                mapping.setProductGroup(null);
                productGroupMappingRepository.delete(mapping);
            }
            product.setGroupMappings(new ArrayList<>());

            // Xóa ProductDiscount
            List<ProductDiscount> discounts = new ArrayList<>(product.getDiscounts());
            for (ProductDiscount discount : discounts) {
                discount.setProduct(null);
                productDiscountRepository.delete(discount);
            }
            product.setDiscounts(new ArrayList<>());

            // Xóa ProductOption và VariantOptionMapping liên quan
            List<ProductOption> options = new ArrayList<>(product.getProductOptions());
            for (ProductOption option : options) {
                // Xóa tất cả VariantOptionMapping liên quan đến option này
                variantOptionMappingRepository.deleteByProductOptionId(option.getId());

                option.setProduct(null);
                productOptionRepository.delete(option);
            }
            product.setProductOptions(new ArrayList<>());

            // Xóa ProductVariant và Inventory liên quan
            List<ProductVariant> variants = new ArrayList<>(product.getVariants());
            for (ProductVariant variant : variants) {
                // Xóa VariantOptionMapping liên quan đến variant này
                variantOptionMappingRepository.deleteByVariantId(variant.getId());

                // Xóa Inventory nếu có
                if (variant.getInventory() != null) {
                    variant.getInventory().setVariant(null);
                    inventoryRepository.delete(variant.getInventory());
                    variant.setInventory(null);
                }

                variant.setProduct(null);
                productVariantRepository.delete(variant);
            }
            product.setVariants(new ArrayList<>());

            // Xóa ProductImage
            List<ProductImage> images = new ArrayList<>(product.getImages());
            for (ProductImage image : images) {
                image.setProduct(null);
                productImageRepository.delete(image);
            }
            product.setImages(new ArrayList<>());

            // Xóa các mối liên kết phụ thuộc
            product.setCategory(null);
            product.setStore(null);

            // Lưu product sau khi đã xóa tất cả các mối quan hệ
            product = productRepository.saveAndFlush(product);

            // Bước 3: Xóa sản phẩm
            productRepository.delete(product);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public Page<Product> getProductsByStore(Long storeId, Pageable pageable) {
        return productRepository.findByStoreId(storeId,pageable);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public ProductDTO getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setCategory(categoryService.convertToDTO(product.getCategory()));
        dto.setStoreId(product.getStore().getId());

        dto.setImages(product.getImages().stream()
                .map(image -> {
                    ProductImageDTO imageDTO = new ProductImageDTO();
                    imageDTO.setId(image.getId());
                    imageDTO.setImageURL(image.getImageURL());
                    imageDTO.setDefault(image.isDefault());
                    return imageDTO;
                })
                .collect(Collectors.toList()));

        dto.setGroups(product.getGroupMappings().stream()
                .map(mapping -> {
                    ProductGroup group = mapping.getProductGroup();
                    ProductGroupDTO groupDTO = new ProductGroupDTO();
                    groupDTO.setId(group.getId());
                    groupDTO.setType(group.getType());
                    groupDTO.setProductCount(group.getProductMappings().size());
                    return groupDTO;
                })
                .collect(Collectors.toList()));

        dto.setOptions(product.getProductOptions().stream()
                .map(po -> {
                    ProductOptionDTO optionDTO = new ProductOptionDTO();
                    optionDTO.setOptionId(po.getOption().getId());
                    optionDTO.setOptionName(po.getOption().getName());
                    optionDTO.setValue(po.getValue());
                    return optionDTO;
                })
                .collect(Collectors.toList()));

        dto.setVariants(product.getVariants().stream()
                .map(variant -> {
                    ProductVariantDTO variantDTO = new ProductVariantDTO();
                    variantDTO.setId(variant.getId());
                    variantDTO.setName(variant.getName());
                    variantDTO.setSku(variant.getSku());
                    variantDTO.setPrice(variant.getPrice());
                    variantDTO.setImg(variant.getImg());
                    variantDTO.setQuantity(variant.getInventory().getQuantity());
                    variantDTO.setOptionIds(variant.getOptionMappings().stream()
                            .map(mapping -> mapping.getProductOption().getId())
                            .collect(Collectors.toList()));
                    return variantDTO;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    @Override
    public List<Product> getProductsByGroupType(GroupType groupType) {
        ProductGroup group = productGroupRepository.findByType(groupType)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with type: " + groupType));

        return productGroupMappingRepository.findByProductGroupId(group.getId())
                .stream()
                .map(ProductGroupMapping::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addProductToGroup(Long productId, GroupType groupType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductGroup group = productGroupRepository.findByType(groupType)
                .orElseGet(() -> {
                    ProductGroup newGroup = new ProductGroup();
                    newGroup.setType(groupType);
                    newGroup.setProductMappings(new ArrayList<>());
                    return productGroupRepository.save(newGroup);
                });

        if (productGroupMappingRepository.findByProductIdAndProductGroupId(productId, group.getId()).isPresent()) {
            return;
        }

        ProductGroupMapping mapping = new ProductGroupMapping();
        mapping.setProduct(product);
        mapping.setProductGroup(group);
        mapping.setAddedDate(LocalDateTime.now());
        productGroupMappingRepository.save(mapping);
    }

    @Override
    @Transactional
    public void removeProductFromGroup(Long productId, GroupType groupType) {
        ProductGroup group = productGroupRepository.findByType(groupType)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with type: " + groupType));

        ProductGroupMapping mapping = productGroupMappingRepository
                .findByProductIdAndProductGroupId(productId, group.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not in group: " + groupType));

        productGroupMappingRepository.delete(mapping);
    }

    @Override
    public List<GroupType> getGroupTypesByProductId(Long productId) {
        return productGroupMappingRepository.findByProductId(productId)
                .stream()
                .map(mapping -> mapping.getProductGroup().getType())
                .collect(Collectors.toList());
    }

    @Override
    public Page<Product> getProductsByGroupTypePaginated(GroupType groupType, Pageable pageable) {
        ProductGroup group = productGroupRepository.findByType(groupType)
                .orElseThrow(() -> new ResourceNotFoundException("Product group not found with type: " + groupType));

        return productRepository.findByGroupType(groupType, pageable);
    }

    // Private helper methods

    private Product initializeProduct(ProductInputDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Store store = storeRepository.findById(productDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + productDTO.getStoreId()));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setCategory(category);
        product.setStore(store);
        product.setProductOptions(new ArrayList<>());
        product.setVariants(new ArrayList<>());
        product.setImages(new ArrayList<>());
        product.setDiscounts(new ArrayList<>());
        product.setGroupMappings(new ArrayList<>());
        return product;
    }

    private void updateProductDetails(Product product, ProductInputDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Store store = storeRepository.findById(productDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + productDTO.getStoreId()));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setCategory(category);
        product.setStore(store);
    }

    private void clearExistingData(Product product) {
//        // Xóa tất cả liên kết nhóm sản phẩm
//        productGroupMappingRepository.deleteByProductId(product.getId());
//
//        // Xóa tất cả tùy chọn sản phẩm
//        productOptionRepository.deleteByProductId(product.getId());
//
//        // Xóa tất cả biến thể sản phẩm và dữ liệu liên quan
//        for (ProductVariant variant : product.getVariants()) {
//            // Xóa thông tin tồn kho
//            if (variant.getInventory() != null) {
//                inventoryRepository.delete(variant.getInventory());
//            }
//
//            // Xóa ánh xạ tùy chọn biến thể
//            variantOptionMappingRepository.deleteByVariantId(variant.getId());
//
//            // Xóa biến thể
//            productVariantRepository.delete(variant);
//        }
        //--------------------------//
        // 1. Xóa ánh xạ tùy chọn biến thể TRƯỚC
        for (ProductVariant variant : product.getVariants()) {
            variantOptionMappingRepository.deleteByVariantId(variant.getId());
        }

        // 2. Xóa tất cả biến thể và tồn kho
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getInventory() != null) {
                inventoryRepository.delete(variant.getInventory());
            }
            productVariantRepository.delete(variant);
        }

        // 3. Xóa tất cả tùy chọn sản phẩm (sau khi không còn bị ràng buộc bởi variant_option_mapping)
        productOptionRepository.deleteByProductId(product.getId());

        // 4. Xóa liên kết nhóm sản phẩm
        productGroupMappingRepository.deleteByProductId(product.getId());
//
        // Xóa các danh sách tham chiếu
//        product.getImages().clear();
//        product.getProductOptions().clear();
//        product.getVariants().clear();
//        product.getGroupMappings().clear();
    }

    private void saveProductImages(Product product, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        product.setImages(imageUrls.stream()
                .map(url -> {
                    ProductImage image = new ProductImage();
                    image.setImageURL(url);
                    image.setProduct(product);
                    image.setDefault(imageUrls.indexOf(url) == 0);
                    return image;
                })
                .collect(Collectors.toList()));
    }

    private void saveProductGroups(Product product, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }

        for (Long groupId : groupIds) {
            ProductGroup group = productGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product group not found with id: " + groupId));

            ProductGroupMapping mapping = new ProductGroupMapping();
            mapping.setProduct(product);
            mapping.setProductGroup(group);
            mapping.setAddedDate(LocalDateTime.now());
            productGroupMappingRepository.save(mapping);
            product.getGroupMappings().add(mapping);
        }
    }

    private void saveProductOptions(Product product, List<OptionInputDTO> options, List<VariantInputDTO> variants) {
        // Xử lý tùy chọn màu sắc từ danh sách options
        if (options != null) {
            for (OptionInputDTO optionInput : options) {
                Option option = optionRepository.findByName(optionInput.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Option not found with name: " + optionInput.getName()));

                for (String value : optionInput.getValues()) {
                    String trimmedValue = value.trim();

                    // Kiểm tra và tạo tùy chọn sản phẩm nếu chưa tồn tại
                    if (productOptionRepository.findByProductIdAndOptionIdAndValue(
                            product.getId(), option.getId(), trimmedValue).isEmpty()) {

                        ProductOption productOption = new ProductOption();
                        productOption.setValue(trimmedValue);
                        productOption.setOption(option);
                        productOption.setProduct(product);
                        productOption.setVariantMappings(new ArrayList<>());
                        productOptionRepository.save(productOption);
                        product.getProductOptions().add(productOption);
                    }
                }
            }
        }

        // Xử lý tùy chọn kích cỡ từ các biến thể
        if (variants != null) {
            Option sizeOption = optionRepository.findByName("Kích cỡ")
                    .orElseThrow(() -> new ResourceNotFoundException("Option not found with name: Kích cỡ"));

            // Thu thập các kích cỡ duy nhất
            Set<String> uniqueSizes = new HashSet<>();
            for (VariantInputDTO variantInput : variants) {
                if (variantInput.getSizes() != null) {
                    for (SizeDTO sizeDTO : variantInput.getSizes()) {
                        uniqueSizes.add(sizeDTO.getSize().trim());
                    }
                }
            }

            // Lưu từng kích cỡ như một tùy chọn sản phẩm
            for (String size : uniqueSizes) {
                if (productOptionRepository.findByProductIdAndOptionIdAndValue(
                        product.getId(), sizeOption.getId(), size).isEmpty()) {

                    ProductOption productOption = new ProductOption();
                    productOption.setValue(size);
                    productOption.setOption(sizeOption);
                    productOption.setProduct(product);
                    productOption.setVariantMappings(new ArrayList<>());
                    productOptionRepository.save(productOption);
                    product.getProductOptions().add(productOption);
                }
            }
        }
    }

    private void saveProductVariants(Product product, List<VariantInputDTO> variants) {
        if (variants == null || variants.isEmpty()) {
            return;
        }

        // Lấy thông tin về các option màu sắc và kích cỡ
        Option colorOption = optionRepository.findByName("Màu sắc")
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with name: Màu sắc"));
        Option sizeOption = optionRepository.findByName("Kích cỡ")
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with name: Kích cỡ"));

        // Xử lý từng biến thể
        for (VariantInputDTO variantInput : variants) {
            String color = variantInput.getColor().trim();

            // Tìm option màu sắc cho sản phẩm này
            ProductOption productColorOption = productOptionRepository
                    .findByProductIdAndOptionIdAndValue(product.getId(), colorOption.getId(), color)
                    .orElseThrow(() -> new ResourceNotFoundException("Color option not found: " + color));

            // Tổng hợp số lượng cho các kích cỡ trùng lặp
            Map<String, Integer> sizeQuantityMap = new HashMap<>();
            if (variantInput.getSizes() != null) {
                for (SizeDTO sizeDTO : variantInput.getSizes()) {
                    String size = sizeDTO.getSize().trim();
                    int quantity = Integer.parseInt(sizeDTO.getQuantity());
                    sizeQuantityMap.merge(size, quantity, Integer::sum);
                }
            }

            // Tạo biến thể cho mỗi kích cỡ
            for (Map.Entry<String, Integer> entry : sizeQuantityMap.entrySet()) {
                String size = entry.getKey();
                int quantity = entry.getValue();

                // Tìm option kích cỡ cho sản phẩm này
                ProductOption productSizeOption = productOptionRepository
                        .findByProductIdAndOptionIdAndValue(product.getId(), sizeOption.getId(), size)
                        .orElseThrow(() -> new ResourceNotFoundException("Size option not found: " + size));

                // Tạo biến thể
                String variantName = color + " - " + size;
                String variantSku = variantInput.getSku() + "-" + size;

                // Kiểm tra SKU trùng lặp
                if (productVariantRepository.findBySku(variantSku).isPresent()) {
                    throw new IllegalArgumentException("SKU already exists: " + variantSku);
                }

                // Tạo và lưu biến thể mới
                ProductVariant variant = new ProductVariant();
                variant.setName(variantName);
                variant.setSku(variantSku);
                variant.setPrice(variantInput.getPrice());
                variant.setImg(variantInput.getVariantImage());
                variant.setProduct(product);
                variant.setOptionMappings(new ArrayList<>());
                variant.setCartDetails(new ArrayList<>());
                variant.setOrderDetails(new ArrayList<>());
                variant = productVariantRepository.save(variant);
                product.getVariants().add(variant);

                // Tạo và lưu thông tin tồn kho
                Inventory inventory = new Inventory();
                inventory.setQuantity(quantity);
                inventory.setStore(product.getStore());
                inventory.setVariant(variant);
                inventory = inventoryRepository.save(inventory);
                variant.setInventory(inventory);

                // Liên kết biến thể với các option
                mapVariantToOptions(variant, List.of(productColorOption, productSizeOption));
            }
        }
    }

    private void mapVariantToOptions(ProductVariant variant, List<ProductOption> productOptions) {
        for (ProductOption productOption : productOptions) {
            VariantOptionMapping mapping = new VariantOptionMapping();
            mapping.setVariant(variant);
            mapping.setProductOption(productOption);
            variantOptionMappingRepository.save(mapping);
            variant.getOptionMappings().add(mapping);
            productOption.getVariantMappings().add(mapping);
        }
    }
}