package com.gamesUP.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequest {
    
    @NotBlank(message = "Le nom du jeu est requis")
    private String name;
    
    private String description;
    
    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être positif")
    private Double price;
    
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playingTime;
    
    @NotNull(message = "La catégorie est requise")
    private Long categoryId;
    
    @NotNull(message = "L'éditeur est requis")
    private Long publisherId;
    
    private List<Long> authorIds;
}