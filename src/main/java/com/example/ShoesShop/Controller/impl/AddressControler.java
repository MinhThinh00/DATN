package com.example.ShoesShop.Controller.impl;


import com.example.ShoesShop.DTO.AddressDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Services.impl.AddressServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressControler {
    private final AddressServiceImpl addressService;

    public AddressControler(AddressServiceImpl addressService) {
        this.addressService = addressService;
    }
    @GetMapping
    ResponseEntity<List<AddressDTO>> getAllAddress() {
        return ResponseEntity.status(200).body(addressService.getAllAddress());
    }
    @GetMapping("/{id}")
    ResponseEntity<AddressDTO> getAddressById(@PathVariable  Long id) {
        return ResponseEntity.status(200).body(addressService.getAddressById(id));
    }
    @PostMapping
    ResponseEntity<ApiResponse> createAddress(@RequestBody AddressDTO address) {
        AddressDTO createdAddress = addressService.createAddress(address);

        ApiResponse response = new ApiResponse(true, "Address created successfully", createdAddress);
        return ResponseEntity.status(201).body(response);

    }
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse> updateAddress(@PathVariable Long id, @RequestBody AddressDTO address) {
        AddressDTO updatedAddress = addressService.updateAddress(id, address);
        ApiResponse response = new ApiResponse(true, "Address created successfully", updatedAddress);
        return ResponseEntity.status(200).body(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long id) {
        try {
            addressService.deleteAddress(id);
            return ResponseEntity.ok("Address deleted successfully with id: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getAddressesByUserId(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

}
