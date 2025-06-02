package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // Custom query methods can be defined here if needed

    @Query("SELECT a FROM Address a WHERE a.user.id = ?1")
    List<Address>  fingByUserId(Long userId);
}
