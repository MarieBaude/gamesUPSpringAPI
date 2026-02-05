package com.gamesUP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gamesUP.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. ROUTES PUBLIQUES (en premier)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**")
                .permitAll()
                
                // 2. AUTH publique
                .requestMatchers("/api/auth/**").permitAll()
                
                // 3. CATEGORIES publiques
                .requestMatchers("/api/categories/**").permitAll()
                
                // 4. GAMES - Lecture publique, modification ADMIN
                .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/games/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/games/**").hasRole("ADMIN")
                
                // 5. USERS - ORDRE IMPORTANT : règles spécifiques AVANT génériques
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                
                // 6. PURCHASES - Authentifié pour tous (CLIENT + ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/purchases").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/purchases").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/purchases/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/purchases/*/status").hasRole("ADMIN")
                
                // 7. ADMIN routes
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 8. Toutes les autres routes nécessitent authentification
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}