package com.gamesUP.repository;

import com.gamesUP.model.Purchase;
import com.gamesUP.model.PurchaseStatus;
import com.gamesUP.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /**
     * Récupérer les achats d'un utilisateur, triés par date décroissante
     */
    List<Purchase> findByUserOrderByPurchaseDateDesc(User user);

    /**
     * Récupérer toutes les commandes triées par date décroissante
     */
    List<Purchase> findAllByOrderByPurchaseDateDesc();

    /**
     * ✅ VALIDATION NOTATION : Vérifier si un utilisateur a acheté un jeu spécifique
     *
     * Parcourt les PurchaseLines pour trouver si le jeu a été acheté
     * par cet utilisateur dans une commande en statut PAID ou DELIVERED.
     * Cette méthode est utilisée par RatingService avant d'autoriser une notation.
     */
    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END " +
           "FROM PurchaseLine pl " +
           "WHERE pl.purchase.user.id = :userId " +
           "AND pl.game.id = :gameId " +
           "AND pl.purchase.status IN :validStatuses")
    boolean existsByUserIdAndGameId(
            @Param("userId") Long userId,
            @Param("gameId") Long gameId,
            @Param("validStatuses") List<PurchaseStatus> validStatuses
    );
}