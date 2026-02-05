package com.gamesUP.repository;

import com.gamesUP.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Trouver un jeu par nom exact
     */
    Optional<Game> findByName(String name);
    
    /**
     * Rechercher des jeux par nom (insensible à la casse)
     */
    List<Game> findByNameContainingIgnoreCase(String name);
    
    /**
     * Filtrer par catégorie
     */
    List<Game> findByCategoryId(Long categoryId);
    
    /**
     * Filtrer par éditeur
     */
    List<Game> findByPublisherId(Long publisherId);
    
    /**
     * Filtrer par auteur
     */
    @Query("SELECT g FROM Game g JOIN g.authors a WHERE a.id = :authorId")
    List<Game> findByAuthorId(@Param("authorId") Long authorId);
    
    /**
     * Recherche multi-critères
     */
    @Query("SELECT DISTINCT g FROM Game g " +
           "LEFT JOIN g.authors a " +
           "WHERE (:name IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:categoryId IS NULL OR g.category.id = :categoryId) " +
           "AND (:publisherId IS NULL OR g.publisher.id = :publisherId) " +
           "AND (:authorId IS NULL OR a.id = :authorId)")
    List<Game> searchGames(
        @Param("name") String name,
        @Param("categoryId") Long categoryId,
        @Param("publisherId") Long publisherId,
        @Param("authorId") Long authorId
    );
}
