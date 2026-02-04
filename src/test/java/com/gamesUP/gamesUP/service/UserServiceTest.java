package com.gamesUP.gamesUP.service;

import com.gamesUP.dto.request.UpdateProfileRequest;
import com.gamesUP.dto.response.UserProfileResponse;
import com.gamesUP.model.Role;
import com.gamesUP.model.User;
import com.gamesUP.repository.UserRepository;
import com.gamesUP.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService.
 * Optimisé pour 70% de couverture instructions ET branches.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_CLIENT);
    }

    // ========== getUserProfile ==========

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = userService.getUserProfile(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }

    // ========== getUserProfileByUsername ==========

    @Test
    void getUserProfileByUsername_ShouldReturnUserProfile_WhenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserProfileResponse response = userService.getUserProfileByUsername("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserProfileByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfileByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ========== updateProfile - Branches critiques ==========

    @Test
    void updateProfile_ShouldUpdateUser_WhenValidRequest() {
        // Branch : Nouveau username ET nouveau email (tous deux disponibles)
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("newusername")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileResponse response = userService.updateProfile(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateUser_WhenKeepingSameUsernameAndEmail() {
        // Branch : Garder username ET email identiques (pas de vérification nécessaire)
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("testuser"); // Même username
        request.setEmail("test@example.com"); // Même email

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileResponse response = userService.updateProfile(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUsernameAlreadyTaken() {
        // Branch : Nouveau username MAIS déjà pris
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ce nom d'utilisateur est déjà pris");
    }

    @Test
    void updateProfile_ShouldThrowException_WhenEmailAlreadyTaken() {
        // Branch : Nouveau email MAIS déjà pris
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cet email est déjà utilisé");
    }

    // ========== getAllUsers ==========

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.ROLE_ADMIN);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserProfileResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findAll();
    }

    // ========== deleteUser ==========

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}