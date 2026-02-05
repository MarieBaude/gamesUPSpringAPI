package com.gamesUP.repository;

import com.gamesUP.model.Purchase;
import com.gamesUP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserOrderByPurchaseDateDesc(User user);
    List<Purchase> findAllByOrderByPurchaseDateDesc();
}
