package com.gamesUP.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreatePurchaseRequest {

    @NotEmpty(message = "La commande doit contenir au moins un jeu")
    private List<PurchaseLineRequest> lines;

    @Data
    public static class PurchaseLineRequest {

        @NotNull(message = "L'ID du jeu est requis")
        private Long gameId;

        @NotNull(message = "La quantité est requise")
        @Min(value = 1, message = "La quantité doit être au moins 1")
        private Integer quantity;
    }
}
