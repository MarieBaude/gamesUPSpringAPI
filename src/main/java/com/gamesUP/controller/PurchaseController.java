package com.gamesUP.controller;

import com.gamesUP.dto.request.CreatePurchaseRequest;
import com.gamesUP.dto.request.UpdatePurchaseStatusRequest;
import com.gamesUP.dto.response.PurchaseResponse;
import com.gamesUP.model.Role;
import com.gamesUP.model.User;
import com.gamesUP.service.PurchaseService;
import com.gamesUP.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Gestion des commandes")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Créer une commande")
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        PurchaseResponse purchase = purchaseService.createPurchase(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
    }

    @GetMapping
    @Operation(summary = "Obtenir les commandes")
    public ResponseEntity<List<PurchaseResponse>> getPurchases(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<PurchaseResponse> purchases;

        if (user.getRole() == Role.ROLE_ADMIN) {
            purchases = purchaseService.getAllPurchases();
        } else {
            purchases = purchaseService.getUserPurchases(user.getId());
        }

        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une commande par ID")
    public ResponseEntity<PurchaseResponse> getPurchaseById(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        PurchaseResponse purchase = purchaseService.getPurchaseById(id, user.getId(), isAdmin);

        return ResponseEntity.ok(purchase);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Modifier le statut d'une commande (ADMIN)")
    public ResponseEntity<PurchaseResponse> updatePurchaseStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePurchaseStatusRequest request) {

        PurchaseResponse purchase = purchaseService.updatePurchaseStatus(id, request);

        return ResponseEntity.ok(purchase);
    }
}
