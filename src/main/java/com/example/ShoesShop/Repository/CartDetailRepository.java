package com.example.ShoesShop.Repository;

import com.example.ShoesShop.Entity.CartDetail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
    List<CartDetail> findByCartId(Long cartId);

    Optional<CartDetail> findByCartIdAndProductVariantId(Long cartId, Long variantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartDetail c WHERE c.cart.id = :cartId")
    void deleteAllByCartId(Long cartId);
}
