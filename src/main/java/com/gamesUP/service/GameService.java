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

    /**
     * Récupérer tous les jeux
     */
    public List<GameResponse> getAllGames() {
        return gameRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un jeu par ID
     */
    public GameResponse getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id));
        return mapToResponse(game);
    }

    /**
     * Rechercher des jeux
     */
    public List<GameResponse> searchGames(String name, Long categoryId, Long publisherId, Long authorId) {
        List<Game> games;
        
        // Si tous les filtres sont null, retourner tous les jeux
        if (name == null && categoryId == null && publisherId == null && authorId == null) {
            games = gameRepository.findAll();
        } else {
            games = gameRepository.searchGames(name, categoryId, publisherId, authorId);
        }
        
        return games.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Créer un nouveau jeu
     */
    @Transactional
    public GameResponse createGame(GameRequest request) {
        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));

        // Vérifier que l'éditeur existe
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Éditeur non trouvé avec l'ID : " + request.getPublisherId()));

        // Récupérer les auteurs
        List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new IllegalArgumentException("Un ou plusieurs auteurs n'ont pas été trouvés");
        }

        // Créer le jeu
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

        Game savedGame = gameRepository.save(game);
        return mapToResponse(savedGame);
    }

    /**
     * Mettre à jour un jeu
     */
    @Transactional
    public GameResponse updateGame(Long id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id));

        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie non trouvée avec l'ID : " + request.getCategoryId()));

        // Vérifier que l'éditeur existe
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new IllegalArgumentException("Éditeur non trouvé avec l'ID : " + request.getPublisherId()));

        // Récupérer les auteurs
        List<Author> authors = authorRepository.findAllById(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new IllegalArgumentException("Un ou plusieurs auteurs n'ont pas été trouvés");
        }

        // Mettre à jour
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
        return mapToResponse(updatedGame);
    }

    /**
     * Supprimer un jeu
     */
    @Transactional
    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new IllegalArgumentException("Jeu non trouvé avec l'ID : " + id);
        }
        gameRepository.deleteById(id);
    }

    /**
     * Mapper Game -> GameResponse
     */
    private GameResponse mapToResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .name(game.getName())
                .description(game.getDescription())
                .price(game.getPrice())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playingTime(game.getPlayingTime())
                .category(mapToCategoryResponse(game.getCategory()))
                .publisher(mapToPublisherResponse(game.getPublisher()))
                .authors(game.getAuthors().stream()
                        .map(this::mapToAuthorResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private PublisherResponse mapToPublisherResponse(Publisher publisher) {
        if (publisher == null) return null;
        return PublisherResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .contactInfo(publisher.getContactInfo())
                .build();
    }

    private AuthorResponse mapToAuthorResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .build();
    }
}