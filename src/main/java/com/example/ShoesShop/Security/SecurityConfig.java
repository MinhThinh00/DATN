package com.example.ShoesShop.Security;


import com.example.ShoesShop.DTO.Auth.LoginResponse;
import com.example.ShoesShop.Services.AuthService;
import com.example.ShoesShop.Services.CustomUserDetailService;
import com.example.ShoesShop.Services.impl.Oauth2;
import com.example.ShoesShop.Utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//
//@Configuration
//@EnableMethodSecurity
//@EnableWebSecurity
//public class SecurityConfig {
//    @Autowired
//    private JwtUtils jwtUtils;
//    @Autowired
//    private ClientRegistrationRepository clientRegistrationRepository;
//
//    @Autowired
//    private CustomUserDetailService customUserDetailService;
//    @Autowired
//    private JwtAuthFilter jwtAuthFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
//        HttpSecurity httpSecurity1 = httpSecurity.csrf(AbstractHttpConfigurer::disable)
//                .cors(Customizer.withDefaults())
//                .authorizeHttpRequests(request -> request
//                        .requestMatchers("/auth/**","/login/**").permitAll()
//                        .anyRequest().authenticated())
//                .oauth2Login(oauth2 -> oauth2 // Thay oauth2Login() bằng cách cấu hình mới
//                        .successHandler((request, response, authentication) -> {
//                            // Xử lý sau khi đăng nhập thành công
//                            response.sendRedirect("/auth/loginGoogle");
//                        })
//                )
//                .logout(logout -> logout
//                        .logoutSuccessHandler(oidcLogoutSuccessHandler()) // Xử lý logout với OIDC
//                        .permitAll()
//                )
//                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// Không sử dụng session
//                .authenticationProvider(authenticationProvider()) // Thêm authentication provider
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);// Thêm JWT filter
//        return httpSecurity.build();
//    }
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        daoAuthenticationProvider.setUserDetailsService(customUserDetailService);
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//        return daoAuthenticationProvider;
//    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//    @Bean
//    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
//        OidcClientInitiatedLogoutSuccessHandler successHandler =
//                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
//        successHandler.setPostLogoutRedirectUri("{baseUrl}/"); // Chuyển hướng sau logout
//        return successHandler;
//    }
//}
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private Oauth2 authService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") // Chỉ áp dụng cho các endpoint API
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/store/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/**", "/login/**", "/oauth2/**") // Áp dụng cho các endpoint OAuth2
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                       // .requestMatchers(HttpMethod.GET,"api/products/**").permitAll()
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            LoginResponse loginResponse = authService.loginWithGoogle((OAuth2AuthenticationToken) authentication);

                            // Redirect về FE kèm token và expiresIn (nếu muốn)
                            String token = URLEncoder.encode(loginResponse.getToken(), StandardCharsets.UTF_8);
                            long expiresIn = loginResponse.getExpiresIn();

                            response.sendRedirect("http://localhost:5173/oauth2/success?token=" + token + "&expiresIn=" + expiresIn);
                            // Sau khi OAuth2 đăng nhập thành công,
                            // bạn có thể tạo JWT và trả về cho client hoặc lưu cookie.
                            //response.sendRedirect("/auth/loginGoogle");
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .permitAll());
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("{baseUrl}/");
        return handler;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
