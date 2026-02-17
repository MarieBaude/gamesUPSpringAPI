package com.gamesUP.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs pour la communication Spring ↔ Python.
 *
 * Format envoyé à Python (POST /recommendations/) :
 * {
 *   "user_id": 1,
 *   "purchases": [
 *     {"game_id": 6, "rating": 8.5},
 *     {"game_id": 2, "rating": 6.0}
 *   ]
 * }
 *
 * Format reçu de Python :
 * {
 *   "user_id": 1,
 *   "recommendations": [
 *     {"game_id": 5, "game_name": "Azul", "score": 4.21},
 *     ...
 *   ]
 * }
 */
public class RecommendationDtos {

    // -------------------------------------------------------------------------
    // Envoyé à Python
    // -------------------------------------------------------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPurchaseRequest {
        @JsonProperty("game_id")
        private Long gameId;

        /** Note sur 10 — même échelle que Python */
        private Double rating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDataRequest {
        @JsonProperty("user_id")
        private Long userId;

        private List<UserPurchaseRequest> purchases;
    }

    // -------------------------------------------------------------------------
    // Reçu de Python
    // -------------------------------------------------------------------------

    /**
     * Enveloppe de la réponse Python :
     * { "user_id": 1, "recommendations": [...] }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonApiResponse {
        @JsonProperty("user_id")
        private Long userId;

        private List<PythonRecommendation> recommendations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PythonRecommendation {
        @JsonProperty("game_id")
        private Long gameId;

        @JsonProperty("game_name")
        private String gameName;

        private Double score;
    }

    // -------------------------------------------------------------------------
    // Retourné à Angular
    // -------------------------------------------------------------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationResponse {
        private Long gameId;
        private String gameName;
        private Double score;       // Score KNN calculé par Python
        private Double averageRating; // Note moyenne réelle en base Spring
        private Double price;         // Prix du jeu en base Spring
    }
}