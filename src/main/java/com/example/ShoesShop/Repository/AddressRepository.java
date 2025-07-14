package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // Custom query methods can be defined here if needed

    @Query("SELECT a FROM Address a WHERE a.user.id = ?1")
    List<Address>  fingByUserId(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void clearDefaultAddressForUser(@Param("userId") Long userId);
}
