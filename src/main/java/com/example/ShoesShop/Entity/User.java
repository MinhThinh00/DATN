package com.example.ShoesShop.Entity;

import com.example.ShoesShop.Enum.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Email not valid")
    @Column(unique = true)
    private String email;

    private String password;

    @NotBlank(message = "Name is required")
    private String userName;

    private String img;

    private Boolean isActive;

    @Column(name = "otp")
    private int otp;

    @Column(name = "otp_expiry_time")
    private Timestamp otpExpiryTime;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(name = "create_at", updatable = false)
    private Timestamp create_at;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getRoleName().name()));
    }

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Address> addresses;

    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    @ToString.Exclude
    private Cart cart;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Order> orders;

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
    public String getFullName() {
        return userName;
    }
}