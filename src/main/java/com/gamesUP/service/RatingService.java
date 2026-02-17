package com.gamesUP.service;

import com.gamesUP.dto.request.RatingRequest;
import com.gamesUP.dto.response.GameRatingStats;
import com.gamesUP.dto.response.RatingResponse;
import com.gamesUP.model.Game;
import com.gamesUP.model.PurchaseStatus;
import com.gamesUP.model.Rating;
import com.gamesUP.model.User;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.PurchaseRepository;
import com.gamesUP.repository.RatingRepository;
import com.gamesUP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant les notes des utilisateurs sur les jeux.
 *
 * Règle métier : un utilisateur ne peut noter un jeu QUE s'il l'a acheté
 * (commande en statut PAID).
 */
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * Statuts de commande considérés comme "achat validé".
     * Adaptez cette liste si votre enum PurchaseStatus a d'autres valeurs
     * (ex : DELIVERED, COMPLETED, etc.)
     */
    private static final List<PurchaseStatus> VALID_PURCHASE_STATUSES = List.of(
            PurchaseStatus.PAID
    );

    /**
     * Créer une nouvelle note.
     * Validation : l'utilisateur doit avoir acheté le jeu (statut PAID).
     */
    @Transactional
    public RatingResponse createRating(Long userId, RatingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + userId));

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + request.getGameId()));

        // ⚠️ VALIDATION MÉTIER : Vérifier que l'utilisateur a acheté le jeu
        boolean hasPurchased = purchaseRepository.existsByUserIdAndGameId(
                userId, request.getGameId(), VALID_PURCHASE_STATUSES
        );
        if (!hasPurchased) {
            throw new IllegalStateException("Vous devez avoir acheté ce jeu pour pouvoir le noter");
        }

        // Vérifier qu'il n'a pas déjà noté ce jeu
        if (ratingRepository.existsByUserIdAndGameId(userId, request.getGameId())) {
            throw new IllegalStateException(
                    "Vous avez déjà noté ce jeu. Utilisez la modification pour changer votre note."
            );
        }

        Rating rating = Rating.builder()
                .score(request.getScore())
                .comment(request.getComment())
                .user(user)
                .game(game)
                .build();

        return RatingResponse.fromEntity(ratingRepository.save(rating));
    }

    /**
     * Mettre à jour une note existante.
     * Seul l'auteur de la note peut la modifier.
     */
    @Transactional
    public RatingResponse updateRating(Long userId, Long ratingId, RatingRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Note non trouvée avec l'ID : " + ratingId));

        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Vous ne pouvez modifier que vos propres notes");
        }

        if (!rating.getGame().getId().equals(request.getGameId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas changer le jeu d'une note existante");
        }

        rating.setScore(request.getScore());
        rating.setComment(request.getComment());

        return RatingResponse.fromEntity(ratingRepository.save(rating));
    }

    /**
     * Supprimer une note.
     * Seul l'auteur de la note peut la supprimer.
     */
    @Transactional
    public void deleteRating(Long userId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new IllegalArgumentException("Note non trouvée avec l'ID : " + ratingId));

        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Vous ne pouvez supprimer que vos propres notes");
        }

        ratingRepository.deleteById(ratingId);
    }

    /**
     * Récupérer toutes les notes d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        return ratingRepository.findByUserId(userId).stream()
                .map(RatingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer la note d'un utilisateur pour un jeu spécifique.
     * Retourne null si aucune note n'existe.
     */
    @Transactional(readOnly = true)
    public RatingResponse getUserRatingForGame(Long userId, Long gameId) {
        return ratingRepository.findByUserIdAndGameId(userId, gameId)
                .map(RatingResponse::fromEntity)
                .orElse(null);
    }

    /**
     * Récupérer toutes les notes d'un jeu.
     */
    @Transactional(readOnly = true)
    public List<RatingResponse> getGameRatings(Long gameId) {
        return ratingRepository.findByGameId(gameId).stream()
                .map(RatingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques de notation d'un jeu (moyenne + total).
     */
    @Transactional(readOnly = true)
    public GameRatingStats getGameRatingStats(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + gameId));

        Double averageRating = ratingRepository.calculateAverageRatingForGame(gameId);
        Long totalRatings = ratingRepository.countRatingsByGameId(gameId);

        return GameRatingStats.builder()
                .gameId(gameId)
                .gameName(game.getName())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings)
                .build();
    }

    /**
     * Vérifier si l'utilisateur connecté peut noter un jeu.
     * Retourne false si : pas acheté OU déjà noté.
     * Utile pour que le frontend affiche/masque le bouton "Noter".
     */
    @Transactional(readOnly = true)
    public boolean canUserRateGame(Long userId, Long gameId) {
        boolean hasPurchased = purchaseRepository.existsByUserIdAndGameId(
                userId, gameId, VALID_PURCHASE_STATUSES
        );
        boolean hasAlreadyRated = ratingRepository.existsByUserIdAndGameId(userId, gameId);
        return hasPurchased && !hasAlreadyRated;
    }
}