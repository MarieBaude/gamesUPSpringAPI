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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        testCategory.setDescription("Jeux de stratégie");

        testPublisher = new Publisher();
        testPublisher.setId(1L);
        testPublisher.setName("Asmodee");
        testPublisher.setContactInfo("contact@asmodee.com");

        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Antoine Bauza");
        testAuthor.setBiography("Créateur de 7 Wonders");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("7 Wonders");
        testGame.setDescription("Jeu de civilisation");
        testGame.setPrice(39.99);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(7);
        testGame.setPlayingTime(30);
        testGame.setCategory(testCategory);
        testGame.setPublisher(testPublisher);
        testGame.setAuthors(Arrays.asList(testAuthor));
    }

    @Test
    void getAllGames_ShouldReturnListOfGames() {
        // Given
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        // When
        List<GameResponse> games = gameService.getAllGames();

        // Then
        assertThat(games).hasSize(1);
        assertThat(games.get(0).getName()).isEqualTo("7 Wonders");
        assertThat(games.get(0).getPrice()).isEqualTo(39.99);
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void getGameById_ShouldReturnGame_WhenGameExists() {
        // Given
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        // When
        GameResponse game = gameService.getGameById(1L);

        // Then
        assertThat(game).isNotNull();
        assertThat(game.getName()).isEqualTo("7 Wonders");
        assertThat(game.getCategory().getName()).isEqualTo("Stratégie");
        assertThat(game.getPublisher().getName()).isEqualTo("Asmodee");
        assertThat(game.getAuthors()).hasSize(1);
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void getGameById_ShouldThrowException_WhenGameNotFound() {
        // Given
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.getGameById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé avec l'ID : 999");
        verify(gameRepository, times(1)).findById(999L);
    }

    @Test
    void createGame_ShouldCreateGame_WhenValidRequest() {
        // Given
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setDescription("Description");
        request.setPrice(29.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(4);
        request.setPlayingTime(45);
        request.setCategoryId(1L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(authorRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testAuthor));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        // When
        GameResponse game = gameService.createGame(request);

        // Then
        assertThat(game).isNotNull();
        verify(categoryRepository, times(1)).findById(1L);
        verify(publisherRepository, times(1)).findById(1L);
        verify(authorRepository, times(1)).findAllById(Arrays.asList(1L));
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void createGame_ShouldThrowException_WhenCategoryNotFound() {
        // Given
        GameRequest request = new GameRequest();
        request.setName("Nouveau jeu");
        request.setPrice(29.99);
        request.setCategoryId(999L);
        request.setPublisherId(1L);
        request.setAuthorIds(Arrays.asList(1L));

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.createGame(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Catégorie non trouvée");
        verify(categoryRepository, times(1)).findById(999L);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void updateGame_ShouldUpdateGame_WhenValidRequest() {
        // Given
        GameRequest request = new GameRequest();
        request.setName("7 Wonders Édité");
        request.setDescription("Nouvelle description");
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

        // When
        GameResponse game = gameService.updateGame(1L, request);

        // Then
        assertThat(game).isNotNull();
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void deleteGame_ShouldDeleteGame_WhenGameExists() {
        // Given
        when(gameRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gameRepository).deleteById(1L);

        // When
        gameService.deleteGame(1L);

        // Then
        verify(gameRepository, times(1)).existsById(1L);
        verify(gameRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteGame_ShouldThrowException_WhenGameNotFound() {
        // Given
        when(gameRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> gameService.deleteGame(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé avec l'ID : 999");
        verify(gameRepository, times(1)).existsById(999L);
        verify(gameRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_WhenCriteriaProvided() {
        // Given
        when(gameRepository.searchGames("7", 1L, null, null)).thenReturn(Arrays.asList(testGame));

        // When
        List<GameResponse> games = gameService.searchGames("7", 1L, null, null);

        // Then
        assertThat(games).hasSize(1);
        assertThat(games.get(0).getName()).isEqualTo("7 Wonders");
        verify(gameRepository, times(1)).searchGames("7", 1L, null, null);
    }

    @Test
    void searchGames_ShouldReturnAllGames_WhenNoCriteriaProvided() {
        // Given
        when(gameRepository.findAll()).thenReturn(Arrays.asList(testGame));

        // When
        List<GameResponse> games = gameService.searchGames(null, null, null, null);

        // Then
        assertThat(games).hasSize(1);
        verify(gameRepository, times(1)).findAll();
        verify(gameRepository, never()).searchGames(any(), any(), any(), any());
    }
}