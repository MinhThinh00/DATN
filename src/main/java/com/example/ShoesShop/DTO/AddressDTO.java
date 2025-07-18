package com.example.ShoesShop.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String address;

    @JsonProperty("isDefault")
    private boolean isDefault;

}
