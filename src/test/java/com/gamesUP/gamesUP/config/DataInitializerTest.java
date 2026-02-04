package com.gamesUP.gamesUP.config;

import com.gamesUP.config.DataInitializer;
import com.gamesUP.model.*;
import com.gamesUP.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour DataInitializer.
 * Vérifie que les données initiales essentielles sont créées.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de configuration - DataInitializer")
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GameRepository gameRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        authorRepository.deleteAll();
        publisherRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        dataInitializer = new DataInitializer(
            userRepository,
            passwordEncoder,
            categoryRepository,
            publisherRepository,
            authorRepository,
            gameRepository
        );
    }

    @Test
    @DisplayName("Devrait créer un utilisateur admin par défaut")
    void shouldCreateDefaultAdminUser() throws Exception {
        dataInitializer.run();

        Optional<User> adminOpt = userRepository.findByUsername("admin");
        assertThat(adminOpt).isPresent();

        User admin = adminOpt.get();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getEmail()).isEqualTo("admin@gamesup.com");
        assertThat(admin.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Devrait créer 3 catégories par défaut")
    void shouldCreateDefaultCategories() throws Exception {
        dataInitializer.run();

        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(categoryRepository.findByName("Stratégie")).isPresent();
        assertThat(categoryRepository.findByName("Famille")).isPresent();
        assertThat(categoryRepository.findByName("Ambiance")).isPresent();
    }

    @Test
    @DisplayName("Devrait créer 2 éditeurs par défaut")
    void shouldCreateDefaultPublishers() throws Exception {
        dataInitializer.run();

        assertThat(publisherRepository.count()).isEqualTo(2);
        assertThat(publisherRepository.findByName("Asmodee")).isPresent();
        assertThat(publisherRepository.findByName("Gigamic")).isPresent();
    }

    @Test
    @DisplayName("Devrait créer 2 auteurs par défaut")
    void shouldCreateDefaultAuthors() throws Exception {
        dataInitializer.run();

        assertThat(authorRepository.count()).isEqualTo(2);
        assertThat(authorRepository.findByName("Reiner Knizia")).isPresent();
        assertThat(authorRepository.findByName("Antoine Bauza")).isPresent();
    }

    @Test
    @DisplayName("Devrait créer 1 jeu d'exemple avec ses relations")
    void shouldCreateDefaultGame() throws Exception {
        dataInitializer.run();

        assertThat(gameRepository.count()).isEqualTo(1);

        // Récupérer le jeu créé (on sait qu'il n'y en a qu'un)
        List<Game> games = gameRepository.findAll();
        assertThat(games).hasSize(1);

        Game game = games.get(0);
        assertThat(game.getName()).isEqualTo("7 Wonders");
        assertThat(game.getPrice()).isEqualTo(39.99);
        assertThat(game.getMinPlayers()).isEqualTo(2);
        assertThat(game.getMaxPlayers()).isEqualTo(7);
        assertThat(game.getPlayingTime()).isEqualTo(30);
        
        // Vérifier les relations
        assertThat(game.getCategory()).isNotNull();
        assertThat(game.getCategory().getName()).isEqualTo("Stratégie");
        assertThat(game.getPublisher()).isNotNull();
        assertThat(game.getPublisher().getName()).isEqualTo("Asmodee");
        assertThat(game.getAuthors()).isNotEmpty();
        assertThat(game.getAuthors().get(0).getName()).isEqualTo("Antoine Bauza");
    }

    @Test
    @DisplayName("Devrait être idempotent (pas de doublons)")
    void shouldBeIdempotent() throws Exception {
        dataInitializer.run();
        dataInitializer.run();
        dataInitializer.run();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(publisherRepository.count()).isEqualTo(2);
        assertThat(authorRepository.count()).isEqualTo(2);
        assertThat(gameRepository.count()).isEqualTo(1);
    }
}
