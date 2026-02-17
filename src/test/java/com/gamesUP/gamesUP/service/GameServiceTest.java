package com.gamesUP.gamesUP.service;

import com.gamesUP.dto.request.GameRequest;
import com.gamesUP.dto.response.GameResponse;
import com.gamesUP.model.Author;
import com.gamesUP.model.Category;
import com.gamesUP.model.Game;
import com.gamesUP.model.Publisher;
import com.gamesUP.repository.*;
import com.gamesUP.service.GameService;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GameService.
 * ⚠️ RatingRepository ajouté car GameService l'injecte désormais
 * pour calculer les statistiques de notation.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private RatingRepository ratingRepository; // ✅ Ajout nécessaire

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Category testCategory;
    private Publisher testPublisher;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Stratégie");

        testPublisher = new Publisher();
        testPublisher.setId(1L);
        testPublisher.setName("Asmodee");

        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Antoine Bauza");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("7 Wonders");
        testGame.setPrice(39.99);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(7);
        testGame.setCategory(testCategory);
        testGame.setPublisher(testPublisher);
        testGame.setAuthors(Arrays.asList(testAuthor));
    }

    // -------------------------------------------------------------------------
    // getAllGames
    // -------------------------------------------------------------------------

    @Test
    void getAllGames_ShouldReturnListWithRatingStats() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));
        when(ratingRepository.calculateAverageRatingForGame(1L)).thenReturn(7.5);
        when(ratingRepository.countRatingsByGameId(1L)).thenReturn(10L);

        List<GameResponse> games = gameService.getAllGames();

        assertThat(games).hasSize(1);
        assertThat(games.get(0).getName()).isEqualTo("7 Wonders");
        assertThat(games.get(0).getAverageRating()).isEqualTo(7.5);
        assertThat(games.get(0).getTotalRatings()).isEqualTo(10L);
        verify(gameRepository).findAll();
    }

    @Test
    void getAllGames_ShouldReturnNullAverageRating_WhenNoRatingsExist() {
        // Branch : pas encore de notes → averageRating null, totalRatings 0
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));
        when(ratingRepository.calculateAverageRatingForGame(1L)).thenReturn(null);
        when(ratingRepository.countRatingsByGameId(1L)).thenReturn(0L);

        List<GameResponse> games = gameService.getAllGames();

        assertThat(games.get(0).getAverageRating()).isNull();
        assertThat(games.get(0).getTotalRatings()).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // getGameById
    // -------------------------------------------------------------------------

    @Test
    void getGameById_ShouldReturnGameWithRating_WhenExists() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingRepository.calculateAverageRatingForGame(1L)).thenReturn(8.0);
        when(ratingRepository.countRatingsByGameId(1L)).thenReturn(5L);

        GameResponse game = gameService.getGameById(1L);

        assertThat(game.getName()).isEqualTo("7 Wonders");
        assertThat(game.getAverageRating()).isEqualTo(8.0);
        assertThat(game.getTotalRatings()).isEqualTo(5L);
    }

    @Test
    void getGameById_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGameById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        // Aucune requête de notation si le jeu n'existe pas
        verifyNoInteractions(ratingRepository);
    }

    // -------------------------------------------------------------------------
    // searchGames — branches null vs non-null
    // -------------------------------------------------------------------------

    @Test
    void searchGames_ShouldCallFindAll_WhenNoCriteria() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));
        when(ratingRepository.calculateAverageRatingForGame(anyLong())).thenReturn(null);
        when(ratingRepository.countRatingsByGameId(anyLong())).thenReturn(0L);

        List<GameResponse> games = gameService.searchGames(null, null, null, null);

        assertThat(games).hasSize(1);
        verify(gameRepository).findAll();
        verify(gameRepository, never()).searchGames(any(), any(), any(), any());
    }

    @Test
    void searchGames_ShouldCallSearchGames_WhenCriteriaProvided() {
        when(gameRepository.searchGames("7", null, null, null))
                .thenReturn(Arrays.asList(testGame));
        when(ratingRepository.calculateAverageRatingForGame(anyLong())).thenReturn(null);
        when(ratingRepository.countRatingsByGameId(anyLong())).thenReturn(0L);

        List<GameResponse> games = gameService.searchGames("7", null, null, null);

        assertThat(games).hasSize(1);
        verify(gameRepository).searchGames("7", null, null, null);
        verify(gameRepository, never()).findAll();
    }

    // -------------------------------------------------------------------------
    // createGame — branches entités manquantes
    // -------------------------------------------------------------------------

    @Test
    void createGame_ShouldReturnGameWithZeroRatings_WhenCreated() {
        GameRequest request = buildValidGameRequest();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testAuthor));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        GameResponse game = gameService.createGame(request);

        assertThat(game).isNotNull();
        assertThat(game.getTotalRatings()).isEqualTo(0L);
        // Pas d'appel au ratingRepository pour un nouveau jeu
        verifyNoInteractions(ratingRepository);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void createGame_ShouldThrowException_WhenCategoryNotFound() {
        GameRequest request = buildValidGameRequest();
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Catégorie non trouvée");

        verify(gameRepository, never()).save(any());
    }

    @Test
    void createGame_ShouldThrowException_WhenPublisherNotFound() {
        GameRequest request = buildValidGameRequest();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Éditeur non trouvé");

        verify(gameRepository, never()).save(any());
    }

    @Test
    void createGame_ShouldThrowException_WhenAuthorsIncomplete() {
        GameRequest request = buildValidGameRequest();
        request.setAuthorIds(Arrays.asList(1L, 999L)); // 2 demandés, 1 trouvé
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(any())).thenReturn(Arrays.asList(testAuthor));

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auteurs n'ont pas été trouvés");

        verify(gameRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateGame — branches
    // -------------------------------------------------------------------------

    @Test
    void updateGame_ShouldUpdateAndReturnWithRating_WhenValid() {
        GameRequest request = buildValidGameRequest();
        request.setName("7 Wonders Édité");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(any())).thenReturn(Arrays.asList(testAuthor));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        when(ratingRepository.calculateAverageRatingForGame(1L)).thenReturn(6.0);
        when(ratingRepository.countRatingsByGameId(1L)).thenReturn(3L);

        GameResponse game = gameService.updateGame(1L, request);

        assertThat(game).isNotNull();
        assertThat(game.getAverageRating()).isEqualTo(6.0);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void updateGame_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.updateGame(999L, buildValidGameRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        verify(gameRepository, never()).save(any());
        verifyNoInteractions(ratingRepository);
    }

    // -------------------------------------------------------------------------
    // deleteGame
    // -------------------------------------------------------------------------

    @Test
    void deleteGame_ShouldDelete_WhenExists() {
        when(gameRepository.existsById(1L)).thenReturn(true);

        gameService.deleteGame(1L);

        verify(gameRepository).deleteById(1L);
    }

    @Test
    void deleteGame_ShouldThrowException_WhenNotFound() {
        when(gameRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> gameService.deleteGame(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        verify(gameRepository, never()).deleteById(anyLong());
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    private GameRequest buildValidGameRequest() {
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setPrice(29.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(4);
        request.setCategoryId(1L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L));
        return request;
    }
}