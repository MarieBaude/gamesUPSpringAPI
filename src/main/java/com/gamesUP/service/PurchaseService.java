package com.gamesUP.service;

import com.gamesUP.dto.request.CreatePurchaseRequest;
import com.gamesUP.dto.request.UpdatePurchaseStatusRequest;
import com.gamesUP.dto.response.GameResponse;
import com.gamesUP.dto.response.PurchaseLineResponse;
import com.gamesUP.dto.response.PurchaseResponse;
import com.gamesUP.model.*;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.PurchaseRepository;
import com.gamesUP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Transactional
    public PurchaseResponse createPurchase(Long userId, CreatePurchaseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + userId));

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setStatus(PurchaseStatus.PAID);

        double total = 0.0;
        for (CreatePurchaseRequest.PurchaseLineRequest lineRequest : request.getLines()) {
            Game game = gameRepository.findById(lineRequest.getGameId())
                    .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + lineRequest.getGameId()));

            PurchaseLine line = new PurchaseLine();
            line.setPurchase(purchase);
            line.setGame(game);
            line.setQuantity(lineRequest.getQuantity());
            line.setUnitPrice(game.getPrice());
            
            total += game.getPrice() * lineRequest.getQuantity();
            purchase.getPurchaseLines().add(line);
        }

        purchase.setTotalAmount(total);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(savedPurchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getUserPurchases(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + userId));

        return purchaseRepository.findByUserOrderByPurchaseDateDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long purchaseId, Long userId, boolean isAdmin) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + purchaseId));

        if (!isAdmin && !purchase.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous n'avez pas accès à cette commande");
        }

        return mapToResponse(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAllByOrderByPurchaseDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseResponse updatePurchaseStatus(Long purchaseId, UpdatePurchaseStatusRequest request) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + purchaseId));

        purchase.setStatus(request.getStatus());
        Purchase updatedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(updatedPurchase);
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .userId(purchase.getUser().getId())
                .username(purchase.getUser().getUsername())
                .lines(purchase.getPurchaseLines().stream()
                        .map(this::mapLineToResponse)
                        .collect(Collectors.toList()))
                .status(purchase.getStatus())
                .totalAmount(purchase.getTotalAmount())
                .purchaseDate(purchase.getPurchaseDate())
                .build();
    }

    private PurchaseLineResponse mapLineToResponse(PurchaseLine line) {
        double subtotal = line.getUnitPrice() * line.getQuantity();
        return PurchaseLineResponse.builder()
                .id(line.getId())
                .game(GameResponse.fromEntity(line.getGame()))
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}
