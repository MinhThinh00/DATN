package com.example.ShoesShop.Services.impl;

import com.example.ShoesShop.DTO.Auth.LoginResponse;
import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Enum.RoleName;
import com.example.ShoesShop.Repository.RoleRepository;
import com.example.ShoesShop.Repository.UserRepository;
import com.example.ShoesShop.Utils.JwtUtils;
import com.example.ShoesShop.exception.OurException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class Oauth2 {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    public LoginResponse loginWithGoogle(OAuth2AuthenticationToken token) {
        try {
            // Lấy thông tin từ Google
            String id = token.getPrincipal().getAttributes().get("sub").toString();
            String email = token.getPrincipal().getAttributes().get("email").toString();
            String name = token.getPrincipal().getAttributes().get("name").toString();
            String picture = token.getPrincipal().getAttributes().get("picture").toString();

            // Kiểm tra và lưu user
            var user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUserName(name);
                newUser.setIsActive(true);
                newUser.setImg(picture);
                Role role = roleRepository.findByRoleName(RoleName.USER).orElseThrow(() -> new OurException("Role not found"));
                newUser.setRole(role);
                return userRepository.save(newUser);
            });


            // Tạo JWT token
            String jwtToken = jwtUtils.generateToken((UserDetails) user,user.getId());
            long expiresIn = jwtUtils.getExpirationTime();

            return new LoginResponse(jwtToken, expiresIn);
        } catch (Exception e) {
            throw new OurException("Không thể đăng nhập bằng google  " + e.getMessage());
        }

    }
}
