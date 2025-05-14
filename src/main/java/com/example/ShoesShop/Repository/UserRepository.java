package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Enum.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String  email);

    @Query(value = "SELECT u FROM User u WHERE u.role.id=1 or u.role.id=2")
    Page<User> findByRole( Pageable pageable);
}
