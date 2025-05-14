package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // Custom query methods can be defined here if needed
}
