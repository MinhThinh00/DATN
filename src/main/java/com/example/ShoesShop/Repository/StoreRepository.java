package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}
