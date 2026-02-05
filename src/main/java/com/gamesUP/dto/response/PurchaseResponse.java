package com.gamesUP.dto.response;

import com.gamesUP.model.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private Long userId;
    private String username;
    private List<PurchaseLineResponse> lines;
    private PurchaseStatus status;
    private Double totalAmount;
    private LocalDateTime purchaseDate;
}
