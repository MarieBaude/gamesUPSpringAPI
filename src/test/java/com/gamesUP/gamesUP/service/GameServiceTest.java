package com.gamesUP.gamesUP.service;

import com.gamesUP.dto.request.GameRequest;
import com.gamesUP.dto.response.GameResponse;
import com.gamesUP.model.Author;
import com.gamesUP.model.Category;
import com.gamesUP.model.Game;
import com.gamesUP.model.Publisher;
import com.gamesUP.repository.AuthorRepository;
import com.gamesUP.repository.CategoryRepository;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.PublisherRepository;
import com.gamesUP.service.GameService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GameService.
 * Optimisé pour 70% de couverture instructions ET branches.
 */
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

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

    // ========== getAllGames ==========

    @Test
    void getAllGames_ShouldReturnListOfGames() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        List<GameResponse> games = gameService.getAllGames();

        assertThat(games).hasSize(1);
        assertThat(games.get(0).getName()).isEqualTo("7 Wonders");
        verify(gameRepository, times(1)).findAll();
    }

    // ========== getGameById ==========

    @Test
    void getGameById_ShouldReturnGame_WhenGameExists() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        GameResponse game = gameService.getGameById(1L);

        assertThat(game).isNotNull();
        assertThat(game.getName()).isEqualTo("7 Wonders");
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void getGameById_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGameById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");
    }

    // ========== searchGames - Branches critiques ==========

    @Test
    void searchGames_ShouldReturnAllGames_WhenNoCriteria() {
        // Branch : Tous les paramètres NULL → findAll()
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        List<GameResponse> games = gameService.searchGames(null, null, null, null);

        assertThat(games).hasSize(1);
        verify(gameRepository, times(1)).findAll();
        verify(gameRepository, never()).searchGames(any(), any(), any(), any());
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_WhenCriteriaProvided() {
        // Branch : Au moins un paramètre NON NULL → searchGames()
        when(gameRepository.searchGames("7", null, null, null))
                .thenReturn(Arrays.asList(testGame));

        List<GameResponse> games = gameService.searchGames("7", null, null, null);

        assertThat(games).hasSize(1);
        assertThat(games.get(0).getName()).isEqualTo("7 Wonders");
        verify(gameRepository, times(1)).searchGames("7", null, null, null);
        verify(gameRepository, never()).findAll();
    }

    // ========== createGame - Branches critiques ==========

    @Test
    void createGame_ShouldCreateGame_WhenAllEntitiesExist() {
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setPrice(29.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(4);
        request.setCategoryId(1L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testAuthor));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        GameResponse game = gameService.createGame(request);

        assertThat(game).isNotNull();
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void createGame_ShouldThrowException_WhenCategoryNotFound() {
        // Branch : Catégorie introuvable
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setCategoryId(999L);
        request.setPublisherId(1L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Catégorie non trouvée");

        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void createGame_ShouldThrowException_WhenPublisherNotFound() {
        // Branch : Publisher introuvable
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setCategoryId(1L);
        request.setPublisherId(999L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Éditeur non trouvé");

        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void createGame_ShouldThrowException_WhenAuthorsNotFound() {
        // Branch : Auteurs incomplets (certains IDs invalides)
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setCategoryId(1L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L, 999L)); // 2 IDs demandés

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(Arrays.asList(1L, 999L)))
                .thenReturn(Arrays.asList(testAuthor)); // Seulement 1 trouvé

        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un ou plusieurs auteurs n'ont pas été trouvés");

        verify(gameRepository, never()).save(any(Game.class));
    }

    // ========== updateGame - Branches critiques ==========

    @Test
    void updateGame_ShouldUpdateGame_WhenValidRequest() {
        GameRequest request = new GameRequest();
        request.setName("7 Wonders Édité");
        request.setPrice(45.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(7);
        request.setPlayingTime(40);
        request.setCategoryId(1L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L));

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testAuthor));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        GameResponse game = gameService.updateGame(1L, request);

        assertThat(game).isNotNull();
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void updateGame_ShouldThrowException_WhenGameNotFound() {
        // Branch : Jeu à modifier introuvable
        GameRequest request = new GameRequest();
        request.setName("Jeu édité");

        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.updateGame(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        verify(gameRepository, never()).save(any(Game.class));
    }

    // ========== deleteGame ==========

    @Test
    void deleteGame_ShouldDeleteGame_WhenGameExists() {
        when(gameRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gameRepository).deleteById(1L);

        gameService.deleteGame(1L);

        verify(gameRepository, times(1)).existsById(1L);
        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteGame_ShouldThrowException_WhenGameNotFound() {
        when(gameRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> gameService.deleteGame(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");

        verify(gameRepository, never()).deleteById(anyLong());
    }
}
