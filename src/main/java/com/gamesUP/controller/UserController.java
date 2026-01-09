package com.gamesUP.controller;

import com.gamesUP.dto.request.UpdateProfileRequest;
import com.gamesUP.dto.response.UserProfileResponse;
import com.gamesUP.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des profils utilisateurs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Récupérer le profil de l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Obtenir mon profil", description = "Récupère le profil de l'utilisateur authentifié")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse profile = userService.getUserProfileByUsername(username);
        return ResponseEntity.ok(profile);
    }

    /**
     * Mettre à jour le profil de l'utilisateur connecté
     */
    @PutMapping("/me")
    @Operation(summary = "Mettre à jour mon profil", description = "Modifie les informations du profil utilisateur")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserProfileResponse currentUser = userService.getUserProfileByUsername(username);
        
        UserProfileResponse updatedProfile = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Récupérer un utilisateur par ID (ADMIN uniquement)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtenir un utilisateur par ID", description = "Réservé aux administrateurs")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * Récupérer tous les utilisateurs (ADMIN uniquement)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs", description = "Réservé aux administrateurs")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Supprimer un utilisateur (ADMIN uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur", description = "Réservé aux administrateurs")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
