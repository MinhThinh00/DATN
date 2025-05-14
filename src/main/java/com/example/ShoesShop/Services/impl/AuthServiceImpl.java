package com.example.ShoesShop.Services.impl;
import com.example.ShoesShop.DTO.Auth.LoginDto;
import com.example.ShoesShop.DTO.Auth.LoginResponse;
import com.example.ShoesShop.DTO.Auth.RegisterDto;
import com.example.ShoesShop.DTO.Auth.VerifyUserDto;
import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Enum.RoleName;
import com.example.ShoesShop.Repository.RoleRepository;
import com.example.ShoesShop.Repository.UserRepository;
import com.example.ShoesShop.Services.AuthService;
import com.example.ShoesShop.Utils.JwtUtils;
import com.example.ShoesShop.exception.OurException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Random;


@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public ResponseEntity<User> register(RegisterDto registerDto) {
        try {
            if (userRepository.existsByEmail(registerDto.getEmail())) {
                throw new OurException("email already exists");
            }
            User user = new User();
            user.setEmail(registerDto.getEmail());
            user.setUserName(registerDto.getUsername());
            Role role = roleRepository.findByRoleName(
                    registerDto.getRole() != null ? RoleName.valueOf(registerDto.getRole()) : RoleName.USER
            ).orElseThrow(() -> new OurException("Role not found"));
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setCreate_at(new Timestamp(System.currentTimeMillis()));
            user.setRole(role);

            int otp = new Random().nextInt(900000) + 100000; // Từ 100000 đến 999999
            user.setOtp(otp);

            long fiveMinutesInMillis = 5 * 60 * 1000; // 5 phút
            user.setOtpExpiryTime(new Timestamp(System.currentTimeMillis() + fiveMinutesInMillis));

            user.setIsActive(false);
            user.setImg(registerDto.getImg());

            User savedUser = userRepository.save(user);
            //send email
//            logic
            sendOtpEmail(savedUser.getEmail(), otp);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            throw new OurException("Error Occurred During User Registration " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> verifyEmail(VerifyUserDto verifyUserDto) {
        try {
            User user = userRepository.findByEmail(verifyUserDto.getEmail())
                    .orElseThrow(() -> new OurException("User not found"));

            if (user.getIsActive()) {
                return ResponseEntity.badRequest().body("User is already activated");
            }

            if (user.getOtp() != Integer.parseInt(verifyUserDto.getOtp())) {
                return ResponseEntity.badRequest().body("Invalid OTP");
            }

            if (user.getOtpExpiryTime().before(new Timestamp(System.currentTimeMillis()))) {
                return ResponseEntity.badRequest().body("OTP has expired");
            }
            user.setIsActive(true);
            user.setOtp(0);
            user.setOtpExpiryTime(null);
            userRepository.save(user);
            return ResponseEntity.ok("Account activated successfully");
        } catch (Exception e) {
            throw new OurException("Error Occurred During OTP Verification " + e.getMessage());
        }
    }
    @Override
    public LoginResponse login(LoginDto loginRequest) {
        try {
            LoginResponse loginResponse= new LoginResponse();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

            var user= userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new OurException("User not found"));
            if (!user.getIsActive()) {
                throw new OurException("Account is not activated. Please verify email.");
            }
            var token = jwtUtils.generateToken((UserDetails) user,user.getId());
            loginResponse.setToken(token);
            loginResponse.setExpiresIn(jwtUtils.getExpirationTime());
            return loginResponse;
        }catch (Exception e){
            throw new OurException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new OurException("User not found"));
            if (!user.getIsActive()) {
                throw new OurException("Account is not activated. Please verify email.");
            }
            int otp = new Random().nextInt(900000) + 100000; // Từ 100000 đến 999999
            user.setOtp(otp);

            long fiveMinutesInMillis = 5 * 60 * 1000; // 5 phút
            user.setOtpExpiryTime(new Timestamp(System.currentTimeMillis() + fiveMinutesInMillis));

            userRepository.save(user);
            sendOtpEmail(email, otp);
            return ResponseEntity.ok("OTP sent to email");
        } catch (Exception e) {
            throw new OurException("Error Occurred During Forgot Password " + e.getMessage());
        }
    }

    @Override
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
                return userRepository.save(newUser);
            });


            // Tạo JWT token
            String jwtToken = jwtUtils.generateToken((UserDetails) user,user.getId());
            long expiresIn = jwtUtils.getExpirationTime();

            return new LoginResponse(jwtToken, expiresIn);
        } catch (Exception e) {
            throw new OurException("Không thể đăng nhập ằng google  " + e.getMessage());
        }

    }
    private void sendOtpEmail(String email, int otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for ShoesShop Registration");
        message.setText("Your OTP is: " + otp + ". It is valid for 5 minutes.");
        mailSender.send(message);
    }
}
