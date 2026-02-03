package com.gamesUP.controller;

import com.gamesUP.dto.request.GameRequest;
import com.gamesUP.dto.response.GameResponse;
import com.gamesUP.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Gestion du catalogue de jeux")
public class GameController {

    private final GameService gameService;

    /**
     * Récupérer tous les jeux (PUBLIC)
     */
    @GetMapping
    @Operation(summary = "Lister tous les jeux", description = "Récupère la liste complète des jeux (accessible publiquement)")
    public ResponseEntity<List<GameResponse>> getAllGames() {
        List<GameResponse> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    /**
     * Rechercher des jeux (PUBLIC)
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des jeux", description = "Recherche multi-critères : nom, catégorie, éditeur, auteur")
    public ResponseEntity<List<GameResponse>> searchGames(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Long authorId) {
        
        List<GameResponse> games = gameService.searchGames(name, categoryId, publisherId, authorId);
        return ResponseEntity.ok(games);
    }

    /**
     * Récupérer un jeu par ID (PUBLIC)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un jeu par ID", description = "Récupère les détails d'un jeu spécifique")
    public ResponseEntity<GameResponse> getGameById(@PathVariable Long id) {
        GameResponse game = gameService.getGameById(id);
        return ResponseEntity.ok(game);
    }

    /**
     * Créer un nouveau jeu (ADMIN uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Créer un jeu", description = "Ajoute un nouveau jeu au catalogue (ADMIN uniquement)")
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameRequest request) {
        GameResponse game = gameService.createGame(request);
        return ResponseEntity.ok(game);
    }

    /**
     * Mettre à jour un jeu (ADMIN uniquement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Mettre à jour un jeu", description = "Modifie un jeu existant (ADMIN uniquement)")
    public ResponseEntity<GameResponse> updateGame(
            @PathVariable Long id,
            @Valid @RequestBody GameRequest request) {
        
        GameResponse game = gameService.updateGame(id, request);
        return ResponseEntity.ok(game);
    }

    /**
     * Supprimer un jeu (ADMIN uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Supprimer un jeu", description = "Supprime un jeu du catalogue (ADMIN uniquement)")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}