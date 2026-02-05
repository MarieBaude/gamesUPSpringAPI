package com.gamesUP.gamesUP.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.dto.request.CreatePurchaseRequest;
import com.gamesUP.dto.request.UpdatePurchaseStatusRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String clientToken;
    private String adminToken;
    private User clientUser;
    private Game testGame;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Stratégie");
        categoryRepository.save(category);

        Publisher publisher = new Publisher();
        publisher.setName("Asmodee");
        publisherRepository.save(publisher);

        testGame = new Game();
        testGame.setName("7 Wonders");
        testGame.setDescription("Jeu de civilisation");
        testGame.setPrice(39.99);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(7);
        testGame.setPlayingTime(30);
        testGame.setCategory(category);
        testGame.setPublisher(publisher);
        gameRepository.save(testGame);

        clientUser = new User();
        clientUser.setUsername("client");
        clientUser.setEmail("client@test.com");
        clientUser.setPassword(passwordEncoder.encode("password"));
        clientUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(clientUser);
        clientToken = jwtUtil.generateToken("client");

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken("admin");
    }

    @Test
    void createPurchase_ShouldCreatePurchase_WhenValidRequest() throws Exception {
        CreatePurchaseRequest request = new CreatePurchaseRequest();
        CreatePurchaseRequest.PurchaseLineRequest line = new CreatePurchaseRequest.PurchaseLineRequest();
        line.setGameId(testGame.getId());
        line.setQuantity(2);
        request.setLines(Arrays.asList(line));

        mockMvc.perform(post("/api/purchases")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(clientUser.getId()))
                .andExpect(jsonPath("$.totalAmount").value(79.98))
                .andExpect(jsonPath("$.status").value("PAID"));

        assert purchaseRepository.count() == 1;
    }

    @Test
    void createPurchase_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        CreatePurchaseRequest request = new CreatePurchaseRequest();

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPurchases_ShouldReturnUserPurchases_WhenClient() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setUser(clientUser);
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(39.99);
        purchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setGame(testGame);
        line.setQuantity(1);
        line.setUnitPrice(39.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);

        purchaseRepository.save(purchase);

        mockMvc.perform(get("/api/purchases")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(clientUser.getId()));
    }

    @Test
    void getPurchases_ShouldReturnAllPurchases_WhenAdmin() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setUser(clientUser);
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(39.99);
        purchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setGame(testGame);
        line.setQuantity(1);
        line.setUnitPrice(39.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);

        purchaseRepository.save(purchase);

        mockMvc.perform(get("/api/purchases")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getPurchaseById_ShouldReturnPurchase_WhenUserOwnsIt() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setUser(clientUser);
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(39.99);
        purchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setGame(testGame);
        line.setQuantity(1);
        line.setUnitPrice(39.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);

        purchase = purchaseRepository.save(purchase);

        mockMvc.perform(get("/api/purchases/" + purchase.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(purchase.getId()));
    }

    @Test
    void updatePurchaseStatus_ShouldUpdateStatus_WhenAdmin() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setUser(clientUser);
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(39.99);
        purchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setGame(testGame);
        line.setQuantity(1);
        line.setUnitPrice(39.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);

        purchase = purchaseRepository.save(purchase);

        UpdatePurchaseStatusRequest request = new UpdatePurchaseStatusRequest();
        request.setStatus(PurchaseStatus.DELIVERED);

        mockMvc.perform(put("/api/purchases/" + purchase.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void updatePurchaseStatus_ShouldReturnForbidden_WhenClient() throws Exception {
        Purchase purchase = new Purchase();
        purchase.setUser(clientUser);
        purchase.setStatus(PurchaseStatus.PAID);
        purchase.setTotalAmount(39.99);
        purchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setGame(testGame);
        line.setQuantity(1);
        line.setUnitPrice(39.99);
        line.setPurchase(purchase);
        purchase.getPurchaseLines().add(line);

        purchase = purchaseRepository.save(purchase);

        UpdatePurchaseStatusRequest request = new UpdatePurchaseStatusRequest();
        request.setStatus(PurchaseStatus.DELIVERED);

        mockMvc.perform(put("/api/purchases/" + purchase.getId() + "/status")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}