package com.gamesUP.gamesUP.service;

import com.gamesUP.dto.request.RatingRequest;
import com.gamesUP.dto.response.GameRatingStats;
import com.gamesUP.dto.response.RatingResponse;
import com.gamesUP.model.*;
import com.gamesUP.repository.*;
import com.gamesUP.service.RatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour RatingService.
 * Couvre les branches critiques : validation achat, unicité de notation,
 * droits de modification/suppression, calcul de statistiques.
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private UserRepository userRepository;
    @Mock private GameRepository gameRepository;
    @Mock private PurchaseRepository purchaseRepository;

    @InjectMocks
    private RatingService ratingService;

    private User testUser;
    private Game testGame;
    private Rating testRating;
    private RatingRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("joueur1");

        testGame = new Game();
        testGame.setId(10L);
        testGame.setName("Azul");

        testRating = Rating.builder()
                .id(100L)
                .score(8.0)
                .comment("Excellent jeu !")
                .user(testUser)
                .game(testGame)
                .build();

        validRequest = new RatingRequest();
        validRequest.setGameId(10L);
        validRequest.setScore(8.0);
        validRequest.setComment("Excellent jeu !");
    }

    // -------------------------------------------------------------------------
    // createRating — branches principales
    // -------------------------------------------------------------------------

    @Test
    void createRating_ShouldCreateRating_WhenUserHasPurchasedGame() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(testGame));
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(true);
        when(ratingRepository.existsByUserIdAndGameId(1L, 10L)).thenReturn(false);
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        RatingResponse response = ratingService.createRating(1L, validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getScore()).isEqualTo(8.0);
        assertThat(response.getGameName()).isEqualTo("Azul");
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void createRating_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(999L, validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void createRating_ShouldThrowException_WhenGameNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(1L, validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void createRating_ShouldThrowException_WhenUserHasNotPurchasedGame() {
        // Branch critique : achat absent → notation refusée
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(testGame));
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(false);

        assertThatThrownBy(() -> ratingService.createRating(1L, validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("devez avoir acheté ce jeu");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void createRating_ShouldThrowException_WhenAlreadyRated() {
        // Branch : jeu déjà noté → refus de doublon
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(10L)).thenReturn(Optional.of(testGame));
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(true);
        when(ratingRepository.existsByUserIdAndGameId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.createRating(1L, validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà noté");

        verify(ratingRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateRating — branches
    // -------------------------------------------------------------------------

    @Test
    void updateRating_ShouldUpdate_WhenOwnerModifies() {
        when(ratingRepository.findById(100L)).thenReturn(Optional.of(testRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(testRating);

        RatingResponse response = ratingService.updateRating(1L, 100L, validRequest);

        assertThat(response).isNotNull();
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void updateRating_ShouldThrowException_WhenRatingNotFound() {
        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.updateRating(1L, 999L, validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Note non trouvée");
    }

    @Test
    void updateRating_ShouldThrowException_WhenNotOwner() {
        // Branch : un autre utilisateur tente de modifier la note
        when(ratingRepository.findById(100L)).thenReturn(Optional.of(testRating));

        assertThatThrownBy(() -> ratingService.updateRating(2L, 100L, validRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ne pouvez modifier que vos propres notes");

        verify(ratingRepository, never()).save(any());
    }

    @Test
    void updateRating_ShouldThrowException_WhenGameIdChanged() {
        // Branch : tentative de changer le jeu d'une note existante
        when(ratingRepository.findById(100L)).thenReturn(Optional.of(testRating));

        RatingRequest wrongGameRequest = new RatingRequest();
        wrongGameRequest.setGameId(99L); // ID différent du jeu d'origine
        wrongGameRequest.setScore(5.0);

        assertThatThrownBy(() -> ratingService.updateRating(1L, 100L, wrongGameRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ne pouvez pas changer le jeu");

        verify(ratingRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteRating — branches
    // -------------------------------------------------------------------------

    @Test
    void deleteRating_ShouldDelete_WhenOwnerDeletes() {
        when(ratingRepository.findById(100L)).thenReturn(Optional.of(testRating));

        ratingService.deleteRating(1L, 100L);

        verify(ratingRepository).deleteById(100L);
    }

    @Test
    void deleteRating_ShouldThrowException_WhenNotOwner() {
        when(ratingRepository.findById(100L)).thenReturn(Optional.of(testRating));

        assertThatThrownBy(() -> ratingService.deleteRating(2L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ne pouvez supprimer que vos propres notes");

        verify(ratingRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRating_ShouldThrowException_WhenRatingNotFound() {
        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.deleteRating(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Note non trouvée");
    }

    // -------------------------------------------------------------------------
    // Lectures (getUserRatings, getGameRatings, getUserRatingForGame)
    // -------------------------------------------------------------------------

    @Test
    void getUserRatings_ShouldReturnAllRatingsOfUser() {
        when(ratingRepository.findByUserId(1L)).thenReturn(Arrays.asList(testRating));

        List<RatingResponse> ratings = ratingService.getUserRatings(1L);

        assertThat(ratings).hasSize(1);
        assertThat(ratings.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getUserRatingForGame_ShouldReturnRating_WhenExists() {
        when(ratingRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.of(testRating));

        RatingResponse response = ratingService.getUserRatingForGame(1L, 10L);

        assertThat(response).isNotNull();
        assertThat(response.getScore()).isEqualTo(8.0);
    }

    @Test
    void getUserRatingForGame_ShouldReturnNull_WhenNotFound() {
        // Branch : pas de note → retour null (frontend gère l'affichage)
        when(ratingRepository.findByUserIdAndGameId(1L, 10L)).thenReturn(Optional.empty());

        RatingResponse response = ratingService.getUserRatingForGame(1L, 10L);

        assertThat(response).isNull();
    }

    // -------------------------------------------------------------------------
    // getGameRatingStats — branches moyenne nulle vs existante
    // -------------------------------------------------------------------------

    @Test
    void getGameRatingStats_ShouldReturnStats_WhenRatingsExist() {
        when(gameRepository.findById(10L)).thenReturn(Optional.of(testGame));
        when(ratingRepository.calculateAverageRatingForGame(10L)).thenReturn(8.5);
        when(ratingRepository.countRatingsByGameId(10L)).thenReturn(12L);

        GameRatingStats stats = ratingService.getGameRatingStats(10L);

        assertThat(stats.getAverageRating()).isEqualTo(8.5);
        assertThat(stats.getTotalRatings()).isEqualTo(12L);
        assertThat(stats.getGameName()).isEqualTo("Azul");
    }

    @Test
    void getGameRatingStats_ShouldReturnZeroAverage_WhenNoRatings() {
        // Branch : calculateAverage retourne null → on renvoie 0.0
        when(gameRepository.findById(10L)).thenReturn(Optional.of(testGame));
        when(ratingRepository.calculateAverageRatingForGame(10L)).thenReturn(null);
        when(ratingRepository.countRatingsByGameId(10L)).thenReturn(0L);

        GameRatingStats stats = ratingService.getGameRatingStats(10L);

        assertThat(stats.getAverageRating()).isEqualTo(0.0);
        assertThat(stats.getTotalRatings()).isEqualTo(0L);
    }

    @Test
    void getGameRatingStats_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.getGameRatingStats(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");
    }

    // -------------------------------------------------------------------------
    // canUserRateGame — toutes les combinaisons de branches
    // -------------------------------------------------------------------------

    @Test
    void canUserRateGame_ShouldReturnTrue_WhenPurchasedAndNotRated() {
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(true);
        when(ratingRepository.existsByUserIdAndGameId(1L, 10L)).thenReturn(false);

        assertThat(ratingService.canUserRateGame(1L, 10L)).isTrue();
    }

    @Test
    void canUserRateGame_ShouldReturnFalse_WhenNotPurchased() {
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(false);
        when(ratingRepository.existsByUserIdAndGameId(1L, 10L)).thenReturn(false);

        assertThat(ratingService.canUserRateGame(1L, 10L)).isFalse();
    }

    @Test
    void canUserRateGame_ShouldReturnFalse_WhenAlreadyRated() {
        when(purchaseRepository.existsByUserIdAndGameId(eq(1L), eq(10L), anyList())).thenReturn(true);
        when(ratingRepository.existsByUserIdAndGameId(1L, 10L)).thenReturn(true);

        assertThat(ratingService.canUserRateGame(1L, 10L)).isFalse();
    }
}