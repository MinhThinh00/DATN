package com.example.ShoesShop.Controller.impl;


import com.example.ShoesShop.DTO.Auth.StaffDto;
import com.example.ShoesShop.DTO.Auth.UserDto;
import com.example.ShoesShop.DTO.OrderDTO;
import com.example.ShoesShop.DTO.response.ApiResponse;
import com.example.ShoesShop.Services.impl.UserServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> AdminCreateStaff(@RequestBody StaffDto staffDto) {
        return ResponseEntity.ok().body(userService.adminCreateUser(staffDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        try {
            UserDto updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse> getStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Page<UserDto> staff = userService.getStaff(pageable);


            Map<String, Object> response = new HashMap<>();
            response.put("staff", staff.getContent());
            response.put("currentPage", staff.getNumber());
            response.put("totalItems", staff.getTotalElements());
            response.put("totalPages", staff.getTotalPages());

            return ResponseEntity.ok(new ApiResponse(true, "successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
