package com.gamesUP.gamesUP.service;

import com.gamesUP.dto.request.CreatePurchaseRequest;
import com.gamesUP.dto.request.UpdatePurchaseStatusRequest;
import com.gamesUP.dto.response.PurchaseResponse;
import com.gamesUP.model.*;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.PurchaseRepository;
import com.gamesUP.repository.UserRepository;
import com.gamesUP.service.PurchaseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private User testUser;
    private Game testGame;
    private Purchase testPurchase;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.ROLE_CLIENT);

        Category category = new Category();
        category.setId(1L);
        category.setName("Stratégie");

        Publisher publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Asmodee");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("7 Wonders");
        testGame.setPrice(39.99);
        testGame.setCategory(category);
        testGame.setPublisher(publisher);

        testPurchase = new Purchase();
        testPurchase.setId(1L);
        testPurchase.setUser(testUser);
        testPurchase.setStatus(PurchaseStatus.PAID);
        testPurchase.setTotalAmount(79.98);
        testPurchase.setPurchaseDate(LocalDateTime.now());

        PurchaseLine line = new PurchaseLine();
        line.setId(1L);
        line.setGame(testGame);
        line.setQuantity(2);
        line.setUnitPrice(39.99);
        line.setPurchase(testPurchase);
        testPurchase.getPurchaseLines().add(line);
    }

    @Test
    void createPurchase_ShouldCreatePurchase_WhenValidRequest() {
        CreatePurchaseRequest request = new CreatePurchaseRequest();
        CreatePurchaseRequest.PurchaseLineRequest lineRequest = new CreatePurchaseRequest.PurchaseLineRequest();
        lineRequest.setGameId(1L);
        lineRequest.setQuantity(2);
        request.setLines(Arrays.asList(lineRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(testPurchase);

        PurchaseResponse response = purchaseService.createPurchase(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualTo(79.98);
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    void createPurchase_ShouldThrowException_WhenUserNotFound() {
        CreatePurchaseRequest request = new CreatePurchaseRequest();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    @Test
    void createPurchase_ShouldThrowException_WhenGameNotFound() {
        CreatePurchaseRequest request = new CreatePurchaseRequest();
        CreatePurchaseRequest.PurchaseLineRequest lineRequest = new CreatePurchaseRequest.PurchaseLineRequest();
        lineRequest.setGameId(999L);
        lineRequest.setQuantity(1);
        request.setLines(Arrays.asList(lineRequest));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jeu non trouvé");
    }

    @Test
    void getUserPurchases_ShouldReturnUserPurchases() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(purchaseRepository.findByUserOrderByPurchaseDateDesc(testUser))
                .thenReturn(Arrays.asList(testPurchase));

        List<PurchaseResponse> purchases = purchaseService.getUserPurchases(1L);

        assertThat(purchases).hasSize(1);
        assertThat(purchases.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getPurchaseById_ShouldReturnPurchase_WhenUserOwnsIt() {
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(testPurchase));

        PurchaseResponse response = purchaseService.getPurchaseById(1L, 1L, false);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getPurchaseById_ShouldThrowException_WhenUserDoesNotOwnIt() {
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(testPurchase));

        assertThatThrownBy(() -> purchaseService.getPurchaseById(1L, 999L, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vous n'avez pas accès à cette commande");
    }

    @Test
    void getAllPurchases_ShouldReturnAllPurchases() {
        when(purchaseRepository.findAllByOrderByPurchaseDateDesc())
                .thenReturn(Arrays.asList(testPurchase));

        List<PurchaseResponse> purchases = purchaseService.getAllPurchases();

        assertThat(purchases).hasSize(1);
    }

    @Test
    void updatePurchaseStatus_ShouldUpdateStatus() {
        UpdatePurchaseStatusRequest request = new UpdatePurchaseStatusRequest();
        request.setStatus(PurchaseStatus.DELIVERED);

        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(testPurchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(testPurchase);

        PurchaseResponse response = purchaseService.updatePurchaseStatus(1L, request);

        assertThat(response).isNotNull();
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }
}