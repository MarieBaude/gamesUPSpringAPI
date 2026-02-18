package com.gamesUP.controller;

import com.gamesUP.dto.recommendation.RecommendationDtos.RecommendationResponse;
import com.gamesUP.service.RecommendationService;
import com.gamesUP.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Recommandations de jeux personnalisées via KNN")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    /**
     * Retourne les recommandations personnalisées pour l'utilisateur connecté.
     *
     * Nécessite d'être authentifié.
     * Retourne une liste vide si :
     *   - L'utilisateur n'a encore noté aucun jeu
     *   - L'API Python est indisponible
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Mes recommandations",
        description = "Retourne les jeux recommandés pour l'utilisateur connecté, " +
                      "basés sur son historique de notes (algorithme KNN via API Python)"
    )
    public ResponseEntity<List<RecommendationResponse>> getMyRecommendations(
            Authentication authentication) {

        Long userId = userService.findByUsername(authentication.getName()).getId();
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
}