package com.gamesUP.dto.request;

import com.gamesUP.model.PurchaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePurchaseStatusRequest {

    @NotNull(message = "Le statut est requis")
    private PurchaseStatus status;
}
