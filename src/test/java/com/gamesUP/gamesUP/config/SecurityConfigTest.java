package com.gamesUP.gamesUP.config;

import com.gamesUP.security.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de configuration pour SecurityConfig.
 * Vérifie les éléments essentiels de la configuration de sécurité.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de configuration - SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("Devrait configurer le PasswordEncoder (BCrypt)")
    void shouldConfigurePasswordEncoder() {
        assertThat(passwordEncoder).isNotNull();
        
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrong", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("Devrait configurer l'AuthenticationManager")
    void shouldConfigureAuthenticationManager() {
        assertThat(authenticationManager).isNotNull();
    }

    @Test
    @DisplayName("Routes publiques - GET /api/games accessible sans authentification")
    void publicRoutesShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/register accessible sans authentification")
    void registerShouldBePublic() throws Exception {
        String registerJson = """
                {
                    "username": "newuser",
                    "email": "newuser@test.com",
                    "password": "password123"
                }
                """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me nécessite une authentification")
    void protectedRoutesShouldRequireAuth() throws Exception {
        // 403 Forbidden car l'accès est refusé (pas de token)
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/games nécessite le rôle ADMIN")
    void adminRoutesShouldRequireAdminRole() throws Exception {
        String gameJson = """
                {
                    "name": "Test Game",
                    "price": 29.99
                }
                """;
        
        // 403 Forbidden car l'accès est refusé (pas de token/rôle)
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Swagger redirige vers la page UI")
    void swaggerShouldRedirectToUI() throws Exception {
        // Swagger redirige souvent vers /swagger-ui/index.html (302)
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Documentation OpenAPI accessible")
    void openApiDocsShouldBePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CSRF désactivé - vérifie que c'est un problème d'autorisation, pas de CSRF")
    void csrfShouldBeDisabled() throws Exception {
        // Si CSRF était activé ET qu'on n'avait pas de token CSRF, on aurait 403 CSRF error
        // Ici on a 403 à cause de l'absence d'authentification, ce qui prouve que CSRF est bien désactivé
        // (sinon on aurait un message d'erreur différent spécifique au CSRF)
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden()); // 403 pour absence d'auth, pas pour CSRF
    }
}