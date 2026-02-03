package com.gamesUP.controller;

import com.gamesUP.dto.response.UserProfileResponse;
import com.gamesUP.dto.response.WishlistItemResponse;
import com.gamesUP.service.UserService;
import com.gamesUP.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Gestion de la liste de souhaits")
@SecurityRequirement(name = "Bearer Authentication")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    /**
     * Récupérer ma wishlist
     */
    @GetMapping
    @Operation(summary = "Obtenir ma wishlist", description = "Récupère tous les jeux de ma liste de souhaits")
    public ResponseEntity<List<WishlistItemResponse>> getMyWishlist(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse user = userService.getUserProfileByUsername(username);
        
        List<WishlistItemResponse> wishlist = wishlistService.getUserWishlist(user.getId());
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Ajouter un jeu à ma wishlist
     */
    @PostMapping("/games/{gameId}")
    @Operation(summary = "Ajouter un jeu à ma wishlist", description = "Ajoute un jeu à ma liste de souhaits")
    public ResponseEntity<WishlistItemResponse> addGameToWishlist(
            Authentication authentication,
            @PathVariable Long gameId) {
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse user = userService.getUserProfileByUsername(username);
        
        WishlistItemResponse item = wishlistService.addGameToWishlist(user.getId(), gameId);
        return ResponseEntity.ok(item);
    }

    /**
     * Retirer un jeu de ma wishlist
     */
    @DeleteMapping("/games/{gameId}")
    @Operation(summary = "Retirer un jeu de ma wishlist", description = "Supprime un jeu de ma liste de souhaits")
    public ResponseEntity<Void> removeGameFromWishlist(
            Authentication authentication,
            @PathVariable Long gameId) {
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse user = userService.getUserProfileByUsername(username);
        
        wishlistService.removeGameFromWishlist(user.getId(), gameId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vérifier si un jeu est dans ma wishlist
     */
    @GetMapping("/games/{gameId}/check")
    @Operation(summary = "Vérifier si un jeu est dans ma wishlist", description = "Retourne true si le jeu est dans la wishlist")
    public ResponseEntity<Boolean> checkGameInWishlist(
            Authentication authentication,
            @PathVariable Long gameId) {
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse user = userService.getUserProfileByUsername(username);
        
        boolean inWishlist = wishlistService.isGameInWishlist(user.getId(), gameId);
        return ResponseEntity.ok(inWishlist);
    }
}