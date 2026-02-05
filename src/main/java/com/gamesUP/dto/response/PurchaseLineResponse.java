package com.gamesUP.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseLineResponse {
    private Long id;
    private GameResponse game;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
}