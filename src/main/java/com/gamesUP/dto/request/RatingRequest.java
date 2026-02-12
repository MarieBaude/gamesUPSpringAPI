package com.gamesUP.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer ou mettre à jour une note
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    @NotNull(message = "L'ID du jeu est requis")
    private Long gameId;

    @NotNull(message = "La note est requise")
    @DecimalMin(value = "0.0", message = "La note doit être comprise entre 0 et 10")
    @DecimalMax(value = "10.0", message = "La note doit être comprise entre 0 et 10")
    private Double score;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String comment;
}