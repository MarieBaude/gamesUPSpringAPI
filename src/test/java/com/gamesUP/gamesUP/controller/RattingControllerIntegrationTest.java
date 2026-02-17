package com.gamesUP.gamesUP.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.dto.request.RatingRequest;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RatingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private String clientToken;
    private String otherClientToken;
    private User testUser;
    private User otherUser;
    private Game testGame;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("client1");
        testUser.setEmail("client1@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(testUser);
        clientToken = jwtUtil.generateToken("client1");

        otherUser = new User();
        otherUser.setUsername("client2");
        otherUser.setEmail("client2@test.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(otherUser);
        otherClientToken = jwtUtil.generateToken("client2");

        Category category = new Category();
        category.setName("Famille");
        categoryRepository.save(category);

        Publisher publisher = new Publisher();
        publisher.setName("Gigamic");
        publisherRepository.save(publisher);

        testGame = new Game();
        testGame.setName("Azul");
        testGame.setPrice(29.99);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(4);
        testGame.setCategory(category);
        testGame.setPublisher(publisher);
        testGame.setAuthors(Arrays.asList());
        gameRepository.save(testGame);

        // Achat PAID pour testUser → peut noter
        PurchaseLine line = new PurchaseLine();
        line.setQuantity(1);
        line.setUnitPrice(29.99);
        line.setGame(testGame);

        Purchase purchase = new Purchase();
        purchase.setUser(testUser);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(29.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);
        purchaseRepository.save(purchase);
        // otherUser n'a PAS acheté le jeu → ne peut pas noter
    }

    // -------------------------------------------------------------------------
    // POST /api/ratings
    // -------------------------------------------------------------------------

    @Test
    void createRating_ShouldReturn201_WhenUserHasPurchasedGame() throws Exception {
        RatingRequest request = new RatingRequest(testGame.getId(), 8.5, "Très bon jeu !");

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(8.5))
                .andExpect(jsonPath("$.gameName").value("Azul"))
                .andExpect(jsonPath("$.userName").value("client1"));
    }

    @Test
    void createRating_ShouldReturn400_WhenUserHasNotPurchasedGame() throws Exception {
        // otherUser n'a pas d'achat → IllegalStateException → 400 via GlobalExceptionHandler
        RatingRequest request = new RatingRequest(testGame.getId(), 7.0, "Commentaire");

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + otherClientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_ShouldReturn400_WhenAlreadyRated() throws Exception {
        // Préparer une note existante en base
        createRatingInDb(testUser, testGame, 7.0, "Première note");

        RatingRequest request = new RatingRequest(testGame.getId(), 9.0, "Deuxième tentative");

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRating_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        RatingRequest request = new RatingRequest(testGame.getId(), 8.0, "Commentaire");

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRating_ShouldReturn400_WhenGameIdIsNull() throws Exception {
        // gameId null → @NotNull déclenché par @Valid → 400 garanti
        // (plus fiable que tester score > 10 qui dépend de la config validation)
        String bodyWithoutGameId = """
                {"score": 8.0, "comment": "Commentaire"}
                """;

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithoutGameId))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/ratings/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateRating_ShouldReturn200_WhenOwnerModifies() throws Exception {
        Rating existing = createRatingInDb(testUser, testGame, 6.0, "Note initiale");

        RatingRequest updateRequest = new RatingRequest(testGame.getId(), 9.0, "Note mise à jour !");

        mockMvc.perform(put("/api/ratings/" + existing.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(9.0))
                .andExpect(jsonPath("$.comment").value("Note mise à jour !"));
    }

    @Test
    void updateRating_ShouldReturn400_WhenNotOwner() throws Exception {
        // La note appartient à testUser, otherUser tente de la modifier → 400
        Rating existing = createRatingInDb(testUser, testGame, 6.0, "Note initiale");

        RatingRequest updateRequest = new RatingRequest(testGame.getId(), 2.0, "Sabotage !");

        mockMvc.perform(put("/api/ratings/" + existing.getId())
                        .header("Authorization", "Bearer " + otherClientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/ratings/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteRating_ShouldReturn204_WhenOwnerDeletes() throws Exception {
        Rating existing = createRatingInDb(testUser, testGame, 7.5, "À supprimer");

        mockMvc.perform(delete("/api/ratings/" + existing.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNoContent());

        assertThat(ratingRepository.findById(existing.getId())).isEmpty();
    }

    @Test
    void deleteRating_ShouldReturn400_WhenNotOwner() throws Exception {
        // La note appartient à testUser, otherUser tente de la supprimer → 400
        Rating existing = createRatingInDb(testUser, testGame, 7.5, "Note protégée");

        mockMvc.perform(delete("/api/ratings/" + existing.getId())
                        .header("Authorization", "Bearer " + otherClientToken))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET — lectures publiques (pas de token requis)
    // ⚠️ Nécessite que /api/ratings/game/** soit public dans SecurityConfig
    // -------------------------------------------------------------------------

    @Test
    void getGameRatings_ShouldReturnAllRatings_WithoutAuthentication() throws Exception {
        createRatingInDb(testUser, testGame, 8.0, "Très bien");

        mockMvc.perform(get("/api/ratings/game/" + testGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].score").value(8.0));
    }

    @Test
    void getGameRatingStats_ShouldReturnStats_WithoutAuthentication() throws Exception {
        createRatingInDb(testUser, testGame, 8.0, "Bien");

        mockMvc.perform(get("/api/ratings/game/" + testGame.getId() + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.totalRatings").value(1))
                .andExpect(jsonPath("$.gameName").value("Azul"));
    }

    @Test
    void canUserRateGame_ShouldReturnTrue_WhenPurchasedAndNotYetRated() throws Exception {
        mockMvc.perform(get("/api/ratings/can-rate/game/" + testGame.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void canUserRateGame_ShouldReturnFalse_WhenAlreadyRated() throws Exception {
        createRatingInDb(testUser, testGame, 7.0, "Déjà noté");

        mockMvc.perform(get("/api/ratings/can-rate/game/" + testGame.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // -------------------------------------------------------------------------
    // Méthode utilitaire
    // -------------------------------------------------------------------------

    private Rating createRatingInDb(User user, Game game, double score, String comment) {
        Rating rating = Rating.builder()
                .score(score)
                .comment(comment)
                .user(user)
                .game(game)
                .build();
        return ratingRepository.save(rating);
    }
}