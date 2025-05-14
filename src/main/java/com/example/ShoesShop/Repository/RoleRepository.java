package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Role;
import com.example.ShoesShop.Enum.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(RoleName roleName);;
}
