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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GameControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private GameRepository gameRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private String adminToken;
    private Category testCategory;
    private Publisher testPublisher;
    private Author testAuthor;
    private Game testGame;

    @BeforeEach
    void setUp() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken("admin");

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

    @Test
    void getAllGames_ShouldReturnGamesWithRatingFields_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"))
                .andExpect(jsonPath("$[0].price").value(39.99))
                .andExpect(jsonPath("$[0].totalRatings").value(0));
    }

    @Test
    void getGameById_ShouldReturnGameWithRatingFields() throws Exception {
        mockMvc.perform(get("/api/games/" + testGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("7 Wonders"))
                .andExpect(jsonPath("$.totalRatings").value(0));
    }

    @Test
    void getGameById_ShouldReturn400_WhenGameNotFound() throws Exception {
        mockMvc.perform(get("/api/games/9999"))
                .andExpect(status().isBadRequest()); // GlobalExceptionHandler → 400
    }

    @Test
    void searchGames_ShouldReturnFilteredGames_ByName() throws Exception {
        mockMvc.perform(get("/api/games/search").param("name", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("7 Wonders"));
    }

    @Test
    void searchGames_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/games/search").param("name", "inexistant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createGame_ShouldReturnCreatedGame_WithZeroRatings_WhenAdmin() throws Exception {
        mockMvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidGameRequest("Nouveau Jeu"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nouveau Jeu"))
                .andExpect(jsonPath("$.totalRatings").value(0));

        assertThat(gameRepository.count()).isEqualTo(2);
    }

    @Test
    void createGame_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidGameRequest("Jeu"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createGame_ShouldReturn400_WhenRequiredFieldsMissing() throws Exception {
        // On envoie un JSON vide : ni name, ni price, ni categoryId → validation échoue
        // ou Hibernate rejette → GlobalExceptionHandler convertit en 400
        String emptyBody = "{}";

        mockMvc.perform(post("/api/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateGame_ShouldUpdateGame_WhenAdmin() throws Exception {
        GameRequest request = buildValidGameRequest("7 Wonders Édité");
        request.setPrice(45.99);

        mockMvc.perform(put("/api/games/" + testGame.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("7 Wonders Édité"));
    }

    @Test
    void updateGame_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/games/" + testGame.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidGameRequest("Edit"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteGame_ShouldDeleteGame_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/games/" + testGame.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(gameRepository.findById(testGame.getId())).isEmpty();
    }

    @Test
    void deleteGame_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/games/" + testGame.getId()))
                .andExpect(status().isForbidden());
    }

    private GameRequest buildValidGameRequest(String name) {
        GameRequest request = new GameRequest();
        request.setName(name);
        request.setDescription("Description de test");
        request.setPrice(29.99);
        request.setMinPlayers(2);
        request.setMaxPlayers(4);
        request.setPlayingTime(45);
        request.setCategoryId(testCategory.getId());
        request.setPublisherId(testPublisher.getId());
        request.setAuthorIds(Arrays.asList(testAuthor.getId()));
        return request;
    }
}