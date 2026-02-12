package com.gamesUP.controller;

import com.gamesUP.dto.request.RatingRequest;
import com.gamesUP.dto.response.GameRatingStats;
import com.gamesUP.dto.response.RatingResponse;
import com.gamesUP.service.RatingService;
import com.gamesUP.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des notes (ratings).
 *
 * Convention : le userId est extrait du token JWT via Authentication,
 * exactement comme dans PurchaseController.
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Gestion des notes des jeux par les utilisateurs")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    // -------------------------------------------------------------------------
    // Endpoints CLIENT (authentification requise)
    // -------------------------------------------------------------------------

    /**
     * Noter un jeu.
     * L'utilisateur doit avoir acheté le jeu (statut PAID).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Noter un jeu",
        description = "Permet à un utilisateur de noter un jeu qu'il a acheté (note de 0 à 10)"
    )
    public ResponseEntity<RatingResponse> createRating(
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        RatingResponse rating = ratingService.createRating(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    /**
     * Modifier sa propre note.
     */
    @PutMapping("/{ratingId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Modifier sa note",
        description = "Modifie une note existante (seul l'auteur peut modifier sa note)"
    )
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable Long ratingId,
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        RatingResponse rating = ratingService.updateRating(userId, ratingId, request);
        return ResponseEntity.ok(rating);
    }

    /**
     * Supprimer sa propre note.
     */
    @DeleteMapping("/{ratingId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Supprimer sa note",
        description = "Supprime une note (seul l'auteur peut supprimer sa note)"
    )
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long ratingId,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        ratingService.deleteRating(userId, ratingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vérifier si l'utilisateur connecté peut noter un jeu.
     * Pratique pour le frontend : afficher/masquer le bouton "Noter".
     */
    @GetMapping("/can-rate/game/{gameId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Peut-on noter ce jeu ?",
        description = "Retourne true si l'utilisateur a acheté le jeu et ne l'a pas encore noté"
    )
    public ResponseEntity<Boolean> canUserRateGame(
            @PathVariable Long gameId,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        return ResponseEntity.ok(ratingService.canUserRateGame(userId, gameId));
    }

    // -------------------------------------------------------------------------
    // Endpoints PUBLICS
    // -------------------------------------------------------------------------

    /**
     * Récupérer toutes les notes d'un utilisateur.
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Notes d'un utilisateur",
        description = "Récupère toutes les notes données par un utilisateur spécifique"
    )
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getUserRatings(userId));
    }

    /**
     * Récupérer la note d'un utilisateur pour un jeu spécifique.
     */
    @GetMapping("/user/{userId}/game/{gameId}")
    @Operation(
        summary = "Note d'un utilisateur pour un jeu",
        description = "Récupère la note donnée par un utilisateur pour un jeu précis"
    )
    public ResponseEntity<RatingResponse> getUserRatingForGame(
            @PathVariable Long userId,
            @PathVariable Long gameId) {

        RatingResponse rating = ratingService.getUserRatingForGame(userId, gameId);
        if (rating == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rating);
    }

    /**
     * Récupérer toutes les notes d'un jeu.
     */
    @GetMapping("/game/{gameId}")
    @Operation(
        summary = "Notes d'un jeu",
        description = "Récupère toutes les notes données à un jeu"
    )
    public ResponseEntity<List<RatingResponse>> getGameRatings(@PathVariable Long gameId) {
        return ResponseEntity.ok(ratingService.getGameRatings(gameId));
    }

    /**
     * Statistiques de notation d'un jeu (note moyenne + total).
     */
    @GetMapping("/game/{gameId}/stats")
    @Operation(
        summary = "Statistiques de notation",
        description = "Note moyenne et nombre total de notes pour un jeu"
    )
    public ResponseEntity<GameRatingStats> getGameRatingStats(@PathVariable Long gameId) {
        return ResponseEntity.ok(ratingService.getGameRatingStats(gameId));
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    /**
     * Extrait l'ID de l'utilisateur connecté depuis le token JWT.
     *
     * À adapter selon votre implémentation de Spring Security.
     * Si votre UserDetails expose l'ID directement :
     *   return ((CustomUserDetails) authentication.getPrincipal()).getId();
     *
     * Sinon, on passe par UserService en cherchant par username (email) :
     *   return userService.findByUsername(authentication.getName()).getId();
     */
    private Long getUserId(Authentication authentication) {
        return userService.findByUsername(authentication.getName()).getId();
    }
}