package com.example.ShoesShop.Services.impl;


import com.example.ShoesShop.DTO.Auth.StaffDto;
import com.example.ShoesShop.DTO.Auth.UserDto;
import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Entity.Store;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Enum.RoleName;
import com.example.ShoesShop.Repository.RoleRepository;
import com.example.ShoesShop.Repository.StoreRepository;
import com.example.ShoesShop.Repository.UserRepository;
import com.example.ShoesShop.exception.OurException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Service
public class UserServiceImpl {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;

    public UserServiceImpl(UserRepository userRepository, UserRepository userRepository1, RoleRepository roleRepository, StoreRepository storeRepository) {
        this.userRepository = userRepository1;
        this.roleRepository = roleRepository;
        this.storeRepository = storeRepository;
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getUsername());
        userDto.setUserName(user.getFullName());
        userDto.setImg(user.getImg());
        userDto.setCreateAt(user.getCreate_at());
        userDto.setIsActive(user.getIsActive());
        userDto.setImg(user.getImg());
        userDto.setRoleName(user.getRole().getRoleName());
        return userDto;
    }

    public UserDto adminCreateUser(StaffDto staffDto) {
        try{
            User user = new User();
            user.setUserName(staffDto.getUserName());
            user.setEmail(staffDto.getEmail());
            Role role = roleRepository.findByRoleName(RoleName.valueOf(String.valueOf(staffDto.getRoleName())))
                    .orElseThrow(() -> new OurException("Role not found"));
            user.setRole(role);
            Store store = storeRepository.findById(staffDto.getStoreId())
                    .orElseThrow(() -> new OurException("Store not found"));
            user.setStore(store);
            user.setIsActive(true);
            user.setPassword(passwordEncoder.encode(staffDto.getPassword()));
            user.setCreate_at(new Timestamp(System.currentTimeMillis()));
            //userRepository.save(user);
            return this.convertToDto( userRepository.save(user));
        }catch (Exception e){
            throw new OurException("Error creating user: " + e.getMessage());
        }
    }
    
    public UserDto updateUser(Long id, UserDto userDto) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found with id: " + id));

            if (userDto.getUserName() != null) {
        
        // Option 1: Hard delete
            }
        
        // Option 2: Soft delete (if you prefer)
        // user.setIsActive(false);
        // userRepository.save(user);

            if (userDto.getEmail() != null) {
                user.setEmail(userDto.getEmail());
            }

            if (userDto.getImg() != null) {
                user.setImg(userDto.getImg());
            }

            if (userDto.getRoleName() != null) {
                Role role = roleRepository.findByRoleName(RoleName.valueOf(String.valueOf(userDto.getRoleName())))
                        .orElseThrow(() -> new OurException("Role not found"));
                user.setRole(role);
            }

            User updatedUser = userRepository.save(user);
            return convertToDto(updatedUser);
        }catch (Exception e){
            throw new OurException("Error updating user: " + e.getMessage());
        }
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new OurException("User not found with id: " + id));

        userRepository.delete(user);
    }
    public Page<UserDto> getStaff(Pageable pageable) {

        Page<User> users = userRepository.findByRole(pageable);
        return users.map(this::convertToDto);
    }
}
