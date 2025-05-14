package com.example.ShoesShop.DTO;
import lombok.Data;

@Data
public class  ProductImageDTO {
    private Long id;
    private String imageURL;
    private boolean isDefault;

}
