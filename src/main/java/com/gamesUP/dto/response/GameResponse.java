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

    /**
     * Convertit une entité Game en GameResponse (DTO)
     * Pattern: Data Transfer Object (DTO)
     * Principe SOLID: Single Responsibility Principle (SRP)
     * 
     * Cette méthode gère la conversion complète incluant les relations:
     * - Category (ManyToOne)
     * - Publisher (ManyToOne)
     * - Authors (ManyToMany)
     * 
     * @param game L'entité Game à convertir
     * @return GameResponse ou null si l'entité est nulle
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
                .build();
    }
}