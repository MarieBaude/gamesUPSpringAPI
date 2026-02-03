package com.gamesUP.service;

import com.gamesUP.dto.request.UpdateProfileRequest;
import com.gamesUP.dto.response.UserProfileResponse;
import com.gamesUP.model.User;
import com.gamesUP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Récupérer le profil d'un utilisateur par son ID
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));
        
        return mapToProfileResponse(user);
    }

    /**
     * Récupérer le profil d'un utilisateur par son username
     */
    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
        
        return mapToProfileResponse(user);
    }

    /**
     * Mettre à jour le profil utilisateur
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        // Vérifier si le username est déjà pris par un autre utilisateur
        if (!user.getUsername().equals(request.getUsername())) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
                    });
        }

        // Vérifier si l'email est déjà pris par un autre utilisateur
        if (!user.getEmail().equals(request.getEmail())) {
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("Cet email est déjà utilisé");
                    });
        }

        // Mise à jour des champs
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    /**
     * Récupérer tous les utilisateurs (ADMIN uniquement)
     */
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer un utilisateur (ADMIN uniquement)
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Mapper User -> UserProfileResponse
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
