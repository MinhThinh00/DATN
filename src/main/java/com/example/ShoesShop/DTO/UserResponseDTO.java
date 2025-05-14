package com.example.ShoesShop.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String img;
    private boolean isActive;
    private String roleName;
    private LocalDateTime createdAt;
    private List<AddressDTO> addresses;

}
