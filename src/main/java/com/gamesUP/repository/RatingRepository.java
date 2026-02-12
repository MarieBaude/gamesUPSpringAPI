package com.gamesUP.repository;

import com.gamesUP.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Trouver une note spécifique d'un utilisateur pour un jeu
     */
    Optional<Rating> findByUserIdAndGameId(Long userId, Long gameId);

    /**
     * Vérifier si un utilisateur a déjà noté un jeu
     */
    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    /**
     * Récupérer toutes les notes d'un utilisateur
     */
    List<Rating> findByUserId(Long userId);

    /**
     * Récupérer toutes les notes d'un jeu
     */
    List<Rating> findByGameId(Long gameId);

    /**
     * Calculer la note moyenne d'un jeu
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.game.id = :gameId")
    Double calculateAverageRatingForGame(@Param("gameId") Long gameId);

    /**
     * Compter le nombre de notes pour un jeu
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.game.id = :gameId")
    Long countRatingsByGameId(@Param("gameId") Long gameId);

    /**
     * Récupérer les dernières notes d'un utilisateur
     */
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Rating> findRecentRatingsByUser(@Param("userId") Long userId);

    /**
     * Récupérer les jeux les mieux notés
     */
    @Query("SELECT r.game.id, AVG(r.score) as avgScore FROM Rating r " +
           "GROUP BY r.game.id " +
           "HAVING COUNT(r) >= :minRatings " +
           "ORDER BY avgScore DESC")
    List<Object[]> findTopRatedGames(@Param("minRatings") Long minRatings);
}