package com.gamesUP.service;

import com.gamesUP.dto.response.WishlistItemResponse;
import com.gamesUP.model.Game;
import com.gamesUP.model.User;
import com.gamesUP.model.Wishlist;
import com.gamesUP.repository.GameRepository;
import com.gamesUP.repository.UserRepository;
import com.gamesUP.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    /**
     * Récupérer la wishlist d'un utilisateur
     */
    public List<WishlistItemResponse> getUserWishlist(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId);
        }

        return wishlistRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ajouter un jeu à la wishlist
     */
    @Transactional
    public WishlistItemResponse addGameToWishlist(Long userId, Long gameId) {
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        // Vérifier que le jeu existe
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID : " + gameId));

        // Vérifier que le jeu n'est pas déjà dans la wishlist
        if (wishlistRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new IllegalArgumentException("Ce jeu est déjà dans votre wishlist");
        }

        // Créer l'entrée wishlist
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setGame(game);
        // addedDate sera rempli automatiquement par @PrePersist

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return mapToResponse(savedWishlist);
    }

    /**
     * Retirer un jeu de la wishlist
     */
    @Transactional
    public void removeGameFromWishlist(Long userId, Long gameId) {
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId);
        }

        // Vérifier que le jeu existe dans la wishlist
        if (!wishlistRepository.existsByUserIdAndGameId(userId, gameId)) {
            throw new IllegalArgumentException("Ce jeu n'est pas dans votre wishlist");
        }

        wishlistRepository.deleteByUserIdAndGameId(userId, gameId);
    }

    /**
     * Vérifier si un jeu est dans la wishlist d'un utilisateur
     */
    public boolean isGameInWishlist(Long userId, Long gameId) {
        return wishlistRepository.existsByUserIdAndGameId(userId, gameId);
    }

    /**
     * Mapper Wishlist -> WishlistItemResponse
     */
    private WishlistItemResponse mapToResponse(Wishlist wishlist) {
        Game game = wishlist.getGame();
        
        return WishlistItemResponse.builder()
                .id(wishlist.getId())
                .gameId(game.getId())
                .gameName(game.getName())
                .gameDescription(game.getDescription())
                .gamePrice(game.getPrice())
                .addedDate(wishlist.getAddedDate())
                .build();
    }
}