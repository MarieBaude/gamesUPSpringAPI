package com.gamesUP.gamesUP.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.dto.request.GameRequest;
import com.gamesUP.model.*;
import com.gamesUP.repository.*;
import com.gamesUP.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private Category testCategory;
    private Publisher testPublisher;
    private Author testAuthor;
    private Game testGame;

    @BeforeEach
    void setUp() {
        // Créer un admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken("admin");

        // Créer des données de test
        testCategory = new Category();
        testCategory.setName("Stratégie");
        testCategory.setDescription("Jeux de stratégie");
        categoryRepository.save(testCategory);

        testPublisher = new Publisher();
        testPublisher.setName("Asmodee");
        testPublisher.setContactInfo("contact@asmodee.com");
        publisherRepository.save(testPublisher);

        testAuthor = new Author();
        testAuthor.setName("Antoine Bauza");
        testAuthor.setBiography("Créateur de 7 Wonders");
        authorRepository.save(testAuthor);

        testGame = new Game();
        testGame.setName("7 Wonders");
        testGame.setDescription("Jeu de civilisation");
        testGame.setPrice(39.99);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(7);
        testGame.setPlayingTime(30);
        testGame.setCategory(testCategory);
        testGame.setPublisher(testPublisher);
        testGame.getAuthors().add(testAuthor);
        gameRepository.save(testGame);
    }

    // ========== Tests GET /api/games (PUBLIC) ==========

    @Test
    void getAllGames_ShouldReturnGamesList_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"))
                .andExpect(jsonPath("$[0].price").value(39.99))
                .andExpect(jsonPath("$[0].category.name").value("Stratégie"))
                .andExpect(jsonPath("$[0].publisher.name").value("Asmodee"))
                .andExpect(jsonPath("$[0].authors", hasSize(1)))
                .andExpect(jsonPath("$[0].authors[0].name").value("Antoine Bauza"));
    }

    // ========== Tests GET /api/games/{id} (PUBLIC) ==========

    @Test
    void getGameById_ShouldReturnGame_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/games/" + testGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("7 Wonders"))
                .andExpect(jsonPath("$.description").value("Jeu de civilisation"))
                .andExpect(jsonPath("$.price").value(39.99))
                .andExpect(jsonPath("$.minPlayers").value(2))
                .andExpect(jsonPath("$.maxPlayers").value(7));
    }

    @Test
    void getGameById_ShouldReturnBadRequest_WhenGameNotFound() throws Exception {
        mockMvc.perform(get("/api/games/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Jeu non trouvé")));
    }

    // ========== Tests GET /api/games/search (PUBLIC) ==========

    @Test
    void searchGames_ShouldReturnFilteredGames_ByName() throws Exception {
        mockMvc.perform(get("/api/games/search")
                        .param("name", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_ByCategory() throws Exception {
        mockMvc.perform(get("/api/games/search")
                        .param("categoryId", testCategory.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_ByPublisher() throws Exception {
        mockMvc.perform(get("/api/games/search")
                        .param("publisherId", testPublisher.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_ByAuthor() throws Exception {
        mockMvc.perform(get("/api/games/search")
                        .param("authorId", testAuthor.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));
    }

    @Test
    void searchGames_ShouldReturnAllGames_WhenNoCriteria() throws Exception {
        mockMvc.perform(get("/api/games/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void searchGames_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/games/search")
                        .param("name", "inexistant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========== Tests POST /api/games (ADMIN uniquement) ==========

    @Test
    void createGame_ShouldCreateGame_WhenAdmin() throws Exception {
        GameRequest request = new GameRequest();
        request.setName("Nouveau Jeu");
        request.setDescription("Description du nouveau jeu");
        request.setPrice(29.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(4);
        request.setPlayingTime(45);
        request.setCategoryId(testCategory.getId());
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));

        mockMvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nouveau Jeu"))
                .andExpect(jsonPath("$.price").value(29.99));

        // Vérifier en base
        assert gameRepository.count() == 2;
    }

    @Test
    void createGame_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        GameRequest request = new GameRequest();
        request.setName("Nouveau Jeu");
        request.setPrice(29.99);
        request.setCategoryId(testCategory.getId());
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createGame_ShouldReturnBadRequest_WhenCategoryNotFound() throws Exception {
        GameRequest request = new GameRequest();
        request.setName("Nouveau Jeu");
        request.setPrice(29.99);
        request.setCategoryId(999L);
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));

        mockMvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Catégorie non trouvée")));
    }

    // ========== Tests PUT /api/games/{id} (ADMIN uniquement) ==========

    @Test
    void updateGame_ShouldUpdateGame_WhenAdmin() throws Exception {
        GameRequest request = new GameRequest();
        request.setName("7 Wonders Édité");
        request.setDescription("Description mise à jour");
        request.setPrice(45.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(7);
        request.setPlayingTime(40);
        request.setCategoryId(testCategory.getId());
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));

        mockMvc.perform(put("/api/games/" + testGame.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("7 Wonders Édité"))
                .andExpect(jsonPath("$.price").value(45.99));

        // Vérifier en base
        Game updatedGame = gameRepository.findById(testGame.getId()).orElseThrow();
        assert updatedGame.getName().equals("7 Wonders Édité");
    }

    @Test
    void updateGame_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        GameRequest request = new GameRequest();
        request.setName("7 Wonders Édité");
        request.setPrice(45.99);
        request.setCategoryId(testCategory.getId());
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));

        mockMvc.perform(put("/api/games/" + testGame.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== Tests DELETE /api/games/{id} (ADMIN uniquement) ==========

    @Test
    void deleteGame_ShouldDeleteGame_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/games/" + testGame.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Vérifier en base
        assert gameRepository.findById(testGame.getId()).isEmpty();
    }

    @Test
    void deleteGame_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/games/" + testGame.getId()))
                .andExpect(status().isForbidden());

        // Vérifier que le jeu n'a pas été supprimé
        assert gameRepository.findById(testGame.getId()).isPresent();
    }

    @Test
    void deleteGame_ShouldReturnBadRequest_WhenGameNotFound() throws Exception {
        mockMvc.perform(delete("/api/games/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Jeu non trouvé")));
    }
}