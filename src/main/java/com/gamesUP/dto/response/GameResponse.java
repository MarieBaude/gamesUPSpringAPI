package com.gamesUP.dto.response;

import com.gamesUP.model.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playingTime;
    private CategoryResponse category;
    private PublisherResponse publisher;
    private List<AuthorResponse> authors;

    private Double averageRating;   // note moyenne sur 10 (null si aucune note)
    private Long totalRatings;       // nombre total de notes

    /**
     * Convertit une entité Game en GameResponse sans statistiques de notation.
     * Utilisé dans les contextes où les notes ne sont pas nécessaires
     * (ex : PurchaseLineResponse, listes internes).
     */
    public static GameResponse fromEntity(Game game) {
        if (game == null) {
            return null;
        }

        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .price(game.getPrice())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playingTime(game.getPlayingTime())
                .category(game.getCategory() != null ?
                        CategoryResponse.fromEntity(game.getCategory()) : null)
                .publisher(game.getPublisher() != null ?
                        PublisherResponse.fromEntity(game.getPublisher()) : null)
                .authors(game.getAuthors() != null ?
                        game.getAuthors().stream()
                                .map(AuthorResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                // Pas de notes ici : null par défaut (champs omis dans la réponse JSON)
                .build();
    }

    /**
     * Convertit une entité Game en GameResponse AVEC les statistiques de notation.
     * Utilisé dans GameService.getGameById() et getAllGames().
     */
    public static GameResponse fromEntityWithRating(Game game, Double averageRating, Long totalRatings) {
        if (game == null) {
            return null;
        }

        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .price(game.getPrice())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playingTime(game.getPlayingTime())
                .category(game.getCategory() != null ?
                        CategoryResponse.fromEntity(game.getCategory()) : null)
                .publisher(game.getPublisher() != null ?
                        PublisherResponse.fromEntity(game.getPublisher()) : null)
                .authors(game.getAuthors() != null ?
                        game.getAuthors().stream()
                                .map(AuthorResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .averageRating(averageRating)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .build();
    }
}