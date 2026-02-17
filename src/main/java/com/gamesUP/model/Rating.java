package com.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant une note donnée par un utilisateur à un jeu
 * Contrainte métier : un utilisateur doit avoir acheté le jeu pour le noter
 * Note sur 10
 */
@Entity
@Table(
    name = "ratings",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "game_id"},
        name = "uk_user_game_rating"
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Note donnée par l'utilisateur (entre 0 et 10)
     */
    @Column(nullable = false)
    private Double score;

    /**
     * Commentaire optionnel de l'utilisateur
     */
    @Column(length = 1000)
    private String comment;

    /**
     * Utilisateur ayant donné la note
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Jeu noté
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     * Date de création de la note
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}