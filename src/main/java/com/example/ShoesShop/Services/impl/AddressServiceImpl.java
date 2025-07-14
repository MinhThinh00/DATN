package com.example.ShoesShop.Services.impl;


import com.example.ShoesShop.DTO.AddressDTO;
import com.example.ShoesShop.Entity.Address;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Repository.AddressRepository;
import com.example.ShoesShop.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public List<AddressDTO> getAllAddress() {
        return addressRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long id) {
        return addressRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    public AddressDTO createAddress(AddressDTO addressDTO) {
        Address address = new Address();
        User user = userRepository.findById(addressDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + addressDTO.getUserId()));
        if (addressDTO.isDefault()) {
            addressRepository.clearDefaultAddressForUser(address.getUser().getId());
        }
        address.setUser(user);
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setDistrict(addressDTO.getDistrict());
        address.setWard(addressDTO.getWard());
        address.setAddress(addressDTO.getAddress());
        address.setDefault(addressDTO.isDefault());

        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long id,AddressDTO addressDTO) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));

        if (addressDTO.isDefault()) {
            addressRepository.clearDefaultAddressForUser(address.getUser().getId());
        }
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setDistrict(addressDTO.getDistrict());
        address.setWard(addressDTO.getWard());
        address.setAddress(addressDTO.getAddress());
        address.setDefault(addressDTO.isDefault());

        Address savedAddress = addressRepository.save(address);
        return convertToDto(savedAddress);
    }
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
        address.setUser(null); // Unlink the address from the user
        addressRepository.save(address);
    }
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        List<Address> addresses =addressRepository.fingByUserId(userId);
        return addresses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private AddressDTO convertToDto(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUser().getId());
        dto.setPhone(address.getPhone());
        dto.setProvince(address.getProvince());
        dto.setDistrict(address.getDistrict());
        dto.setWard(address.getWard());
        dto.setAddress(address.getAddress());
        dto.setDefault(address.isDefault());
        return dto;
    }
}
