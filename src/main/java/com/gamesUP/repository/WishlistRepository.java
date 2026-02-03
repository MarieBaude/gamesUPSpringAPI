package com.gamesUP.repository;

import com.gamesUP.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    /**
     * Récupérer toute la wishlist d'un utilisateur
     */
    List<Wishlist> findByUserId(Long userId);
    
    /**
     * Vérifier si un jeu est déjà dans la wishlist d'un utilisateur
     */
    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * Trouver un élément spécifique de la wishlist
     */
    Optional<Wishlist> findByUserIdAndGameId(Long userId, Long gameId);
    
    /**
     * Supprimer un jeu de la wishlist
     */
    void deleteByUserIdAndGameId(Long userId, Long gameId);
}