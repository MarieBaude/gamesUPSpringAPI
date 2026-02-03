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

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse response = userService.getUserProfile(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_CLIENT);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé avec l'ID : 999");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getUserProfileByUsername_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse response = userService.getUserProfileByUsername("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserProfileByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfileByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé : unknown");

        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    void updateProfile_ShouldUpdateUser_WhenValidRequest() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("newusername")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileResponse response = userService.updateProfile(1L, request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("newusername");
        verify(userRepository, times(1)).findByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUsernameAlreadyTaken() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ce nom d'utilisateur est déjà pris");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenEmailAlreadyTaken() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cet email est déjà utilisé");

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.ROLE_ADMIN);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // When
        List<UserProfileResponse> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser");
        assertThat(users.get(1).getUsername()).isEqualTo("user2");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé avec l'ID : 999");

        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}