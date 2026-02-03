package com.gamesUP.gamesUP.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.dto.request.UpdateProfileRequest;
import com.gamesUP.model.Role;
import com.gamesUP.model.User;
import com.gamesUP.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String clientToken;
    private String adminToken;
    private User clientUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Créer un utilisateur CLIENT
        clientUser = new User();
        clientUser.setUsername("client");
        clientUser.setEmail("client@example.com");
        clientUser.setPassword(passwordEncoder.encode("password"));
        clientUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(clientUser);
        clientToken = jwtUtil.generateToken("client");

        // Créer un utilisateur ADMIN
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(Role.ROLE_ADMIN);
        userRepository.save(adminUser);
        adminToken = jwtUtil.generateToken("admin");
    }

    // ========== Tests GET /api/users/me ==========

    @Test
    void getMyProfile_ShouldReturnUserProfile_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("client"))
                .andExpect(jsonPath("$.email").value("client@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"));
    }

    @Test
    void getMyProfile_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_ShouldReturnUnauthorized_WhenInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());
    }

    // ========== Tests PUT /api/users/me ==========

    @Test
    void updateMyProfile_ShouldUpdateUser_WhenValidRequest() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("newemail@example.com"));

        // Vérifier en base
        User updatedUser = userRepository.findByUsername("newusername").orElseThrow();
        assert updatedUser.getEmail().equals("newemail@example.com");
    }

    @Test
    void updateMyProfile_ShouldReturnBadRequest_WhenUsernameAlreadyTaken() throws Exception {
        // Créer un autre utilisateur
        User otherUser = new User();
        otherUser.setUsername("existinguser");
        otherUser.setEmail("existing@example.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(otherUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("existinguser"); // Username déjà pris
        request.setEmail("client@example.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Ce nom d'utilisateur est déjà pris")));
    }

    @Test
    void updateMyProfile_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ========== Tests GET /api/users (ADMIN uniquement) ==========

    @Test
    void getAllUsers_ShouldReturnUserList_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("client", "admin")));
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenClient() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // ========== Tests GET /api/users/{id} (ADMIN uniquement) ==========

    @Test
    void getUserById_ShouldReturnUser_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/users/" + clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("client"))
                .andExpect(jsonPath("$.email").value("client@example.com"));
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenClient() throws Exception {
        mockMvc.perform(get("/api/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Utilisateur non trouvé")));
    }

    // ========== Tests DELETE /api/users/{id} (ADMIN uniquement) ==========

    @Test
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Vérifier que l'utilisateur a été supprimé
        assert userRepository.findById(clientUser.getId()).isEmpty();
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenClient() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        // Vérifier que l'utilisateur n'a pas été supprimé
        assert userRepository.findById(adminUser.getId()).isPresent();
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/users/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Utilisateur non trouvé")));
    }
}