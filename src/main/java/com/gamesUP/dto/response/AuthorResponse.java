package com.gamesUP.dto.response;

import com.gamesUP.model.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    private Long id;
    private String name;
    private String biography;

    /**
     * Convertit une entité Author en AuthorResponse (DTO)
     * Pattern: Data Transfer Object (DTO)
     * Principe SOLID: Single Responsibility Principle (SRP)
     * 
     * @param author L'entité Author à convertir
     * @return AuthorResponse ou null si l'entité est nulle
     */
    public static AuthorResponse fromEntity(Author author) {
        if (author == null) {
            return null;
        }

        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .build();
    }
}