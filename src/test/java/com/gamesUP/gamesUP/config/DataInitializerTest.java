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

    @Autowired
    private PurchaseRepository purchaseRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
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
            gameRepository,
            purchaseRepository
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
    @DisplayName("Devrait créer un utilisateur client par défaut")
    void shouldCreateDefaultClientUser() throws Exception {
        dataInitializer.run();

        Optional<User> clientOpt = userRepository.findByUsername("client");
        assertThat(clientOpt).isPresent();

        User client = clientOpt.get();
        assertThat(client.getUsername()).isEqualTo("client");
        assertThat(client.getEmail()).isEqualTo("client@gamesup.com");
        assertThat(client.getRole()).isEqualTo(Role.ROLE_CLIENT);
        assertThat(passwordEncoder.matches("client123", client.getPassword())).isTrue();
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
    @DisplayName("Devrait créer 2 jeux d'exemple avec leurs relations")
    void shouldCreateDefaultGame() throws Exception {
        dataInitializer.run();

        assertThat(gameRepository.count()).isEqualTo(2);

        // Vérifier 7 Wonders
        Optional<Game> sevenWondersOpt = gameRepository.findByName("7 Wonders");
        assertThat(sevenWondersOpt).isPresent();
        
        Game sevenWonders = sevenWondersOpt.get();
        assertThat(sevenWonders.getName()).isEqualTo("7 Wonders");
        assertThat(sevenWonders.getPrice()).isEqualTo(39.99);
        assertThat(sevenWonders.getMinPlayers()).isEqualTo(2);
        assertThat(sevenWonders.getMaxPlayers()).isEqualTo(7);
        assertThat(sevenWonders.getPlayingTime()).isEqualTo(30);
        assertThat(sevenWonders.getCategory().getName()).isEqualTo("Stratégie");
        assertThat(sevenWonders.getPublisher().getName()).isEqualTo("Asmodee");
        assertThat(sevenWonders.getAuthors()).isNotEmpty();
        assertThat(sevenWonders.getAuthors().get(0).getName()).isEqualTo("Antoine Bauza");

        // Vérifier Azul
        Optional<Game> azulOpt = gameRepository.findByName("Azul");
        assertThat(azulOpt).isPresent();
        
        Game azul = azulOpt.get();
        assertThat(azul.getName()).isEqualTo("Azul");
        assertThat(azul.getPrice()).isEqualTo(29.99);
        assertThat(azul.getMinPlayers()).isEqualTo(2);
        assertThat(azul.getMaxPlayers()).isEqualTo(4);
        assertThat(azul.getPlayingTime()).isEqualTo(45);
        assertThat(azul.getCategory().getName()).isEqualTo("Famille");
        assertThat(azul.getPublisher().getName()).isEqualTo("Gigamic");
        assertThat(azul.getAuthors()).isNotEmpty();
        assertThat(azul.getAuthors().get(0).getName()).isEqualTo("Reiner Knizia");
    }

    @Test
    @DisplayName("Devrait créer 2 commandes d'exemple")
    void shouldCreateDefaultPurchases() throws Exception {
        dataInitializer.run();

        assertThat(purchaseRepository.count()).isEqualTo(2);

        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(2);

        // Vérifier que les commandes ont des lignes
        for (Purchase purchase : purchases) {
            assertThat(purchase.getPurchaseLines()).isNotEmpty();
            assertThat(purchase.getTotalAmount()).isGreaterThan(0);
            assertThat(purchase.getStatus()).isIn(PurchaseStatus.PAID, PurchaseStatus.DELIVERED);
        }
    }

    @Test
    @DisplayName("Devrait être idempotent (pas de doublons)")
    void shouldBeIdempotent() throws Exception {
        dataInitializer.run();
        dataInitializer.run();
        dataInitializer.run();

        assertThat(userRepository.count()).isEqualTo(2); // admin + client
        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(publisherRepository.count()).isEqualTo(2);
        assertThat(authorRepository.count()).isEqualTo(2);
        assertThat(gameRepository.count()).isEqualTo(2); // 7 Wonders + Azul
        assertThat(purchaseRepository.count()).isEqualTo(2); // 2 commandes
    }
}