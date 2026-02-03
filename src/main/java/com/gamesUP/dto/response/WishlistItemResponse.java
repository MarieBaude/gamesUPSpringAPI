package com.gamesUP.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {
    private Long id;
    private Long gameId;
    private String gameName;
    private String gameDescription;
    private Double gamePrice;
    private LocalDateTime addedDate;
}