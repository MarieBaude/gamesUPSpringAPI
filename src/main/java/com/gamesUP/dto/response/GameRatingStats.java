package com.gamesUP.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques de notation d'un jeu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRatingStats {

    private Long gameId;
    private String gameName;
    private Double averageRating;
    private Long totalRatings;
}