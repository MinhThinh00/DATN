package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Order;
import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Entity.User;
import com.example.ShoesShop.Enum.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String  email);

    @Query(value = "SELECT u FROM User u WHERE u.role.id=1 or u.role.id=2")
    Page<User> findByRole( Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE u.role.id=3")
    Page<User> findCustomer( Pageable pageable);


    @Query("SELECT u FROM User u WHERE " +
            "(:name IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:roleName IS NULL OR u.role IS NULL OR u.role.roleName = :roleName)")
    Page<User> findByFilter(@Param("name") String name,
                            @Param("roleName") RoleName roleName,
                            Pageable pageable);
}
