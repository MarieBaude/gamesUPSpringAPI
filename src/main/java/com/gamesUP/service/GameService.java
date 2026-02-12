package com.gamesUP.service;

import com.gamesUP.dto.request.GameRequest;
import com.gamesUP.dto.response.*;
import com.gamesUP.model.Author;
import com.gamesUP.model.Category;
import com.gamesUP.model.Game;
import com.gamesUP.model.Publisher;
import com.gamesUP.repository.AuthorRepository;
import com.gamesUP.repository.CategoryRepository;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.PublisherRepository;
import com.gamesUP.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final RatingRepository ratingRepository; // ✅ Ajout pour les statistiques de notation

    /**
     * Récupérer tous les jeux avec leurs notes moyennes.
     */
    public List<GameResponse> getAllGames() {
        return gameRepository.findAll().stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un jeu par ID avec sa note moyenne.
     */
    public GameResponse getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id));
        return mapToResponseWithRating(game);
    }

    /**
     * Rechercher des jeux avec leurs notes moyennes.
     */
    public List<GameResponse> searchGames(String name, Long categoryId, Long publisherId, Long authorId) {
        List<Game> games;

        if (name == null && categoryId == null && publisherId == null && authorId == null) {
            games = gameRepository.findAll();
        } else {
            games = gameRepository.searchGames(name, categoryId, publisherId, authorId);
        }

        return games.stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
    }

    /**
     * Créer un nouveau jeu.
     */
    @Transactional
    public GameResponse createGame(GameRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Éditeur non trouvé avec l'ID : " + request.getPublisherId()));

        List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new IllegalArgumentException("Un ou plusieurs auteurs n'ont pas été trouvés");
        }

        Game game = new Game();
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setPrice(request.getPrice());
        game.setMinPlayers(request.getMinPlayers());
        game.setMaxPlayers(request.getMaxPlayers());
        game.setPlayingTime(request.getPlayingTime());
        game.setCategory(category);
        game.setPublisher(publisher);
        game.setAuthors(authors);

        // Nouveau jeu : pas de notes encore, on retourne avec 0
        Game savedGame = gameRepository.save(game);
        return GameResponse.fromEntityWithRating(savedGame, null, 0L);
    }

    /**
     * Mettre à jour un jeu.
     */
    @Transactional
    public GameResponse updateGame(Long id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Éditeur non trouvé avec l'ID : " + request.getPublisherId()));

        List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new IllegalArgumentException("Un ou plusieurs auteurs n'ont pas été trouvés");
        }

        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setPrice(request.getPrice());
        game.setMinPlayers(request.getMinPlayers());
        game.setMaxPlayers(request.getMaxPlayers());
        game.setPlayingTime(request.getPlayingTime());
        game.setCategory(category);
        game.setPublisher(publisher);
        game.setAuthors(authors);

        Game updatedGame = gameRepository.save(game);
        return mapToResponseWithRating(updatedGame);
    }

    /**
     * Supprimer un jeu.
     */
    @Transactional
    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id);
        }
        gameRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Méthodes privées de mapping
    // -------------------------------------------------------------------------

    /**
     * Mappe un jeu vers GameResponse EN récupérant ses statistiques de notation.
     * C'est la méthode principale utilisée par tous les endpoints publics.
     */
    private GameResponse mapToResponseWithRating(Game game) {
        Double averageRating = ratingRepository.calculateAverageRatingForGame(game.getId());
        Long totalRatings = ratingRepository.countRatingsByGameId(game.getId());
        return GameResponse.fromEntityWithRating(game, averageRating, totalRatings);
    }
}