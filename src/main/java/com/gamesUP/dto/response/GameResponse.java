package com.gamesUP.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}