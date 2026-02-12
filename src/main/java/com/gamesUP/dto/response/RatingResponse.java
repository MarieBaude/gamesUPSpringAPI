package com.gamesUP.dto.response;

import com.gamesUP.model.Rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une note
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long id;
    private Double score;
    private String comment;
    private Long userId;
    private String userName;
    private Long gameId;
    private String gameName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convertit une entité Rating en RatingResponse
     */
    public static RatingResponse fromEntity(Rating rating) {
        if (rating == null) {
            return null;
        }

        return RatingResponse.builder()
                .id(rating.getId())
                .score(rating.getScore())
                .comment(rating.getComment())
                .userId(rating.getUser() != null ? rating.getUser().getId() : null)
                .userName(rating.getUser() != null ? rating.getUser().getUsername() : null)
                .gameId(rating.getGame() != null ? rating.getGame().getId() : null)
                .gameName(rating.getGame() != null ? rating.getGame().getName() : null)
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}