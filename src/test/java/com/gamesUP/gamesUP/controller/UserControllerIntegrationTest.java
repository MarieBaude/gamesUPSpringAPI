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

/**
 * Tests d'intégration pour UserController.
 * Version épurée - 7 tests essentiels.
 */
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
        clientUser = new User();
        clientUser.setUsername("client");
        clientUser.setEmail("client@example.com");
        clientUser.setPassword(passwordEncoder.encode("password"));
        clientUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(clientUser);
        clientToken = jwtUtil.generateToken("client");

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(Role.ROLE_ADMIN);
        userRepository.save(adminUser);
        adminToken = jwtUtil.generateToken("admin");
    }

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
    void getMyProfile_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

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
    }

    @Test
    void updateMyProfile_ShouldReturnBadRequest_WhenUsernameAlreadyTaken() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("existinguser");
        otherUser.setEmail("existing@example.com");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole(Role.ROLE_CLIENT);
        userRepository.save(otherUser);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("existinguser");
        request.setEmail("client@example.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("nom d'utilisateur")));
    }

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
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assert userRepository.findById(clientUser.getId()).isEmpty();
    }
}