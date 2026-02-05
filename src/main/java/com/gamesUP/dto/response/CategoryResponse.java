package com.gamesUP.dto.response;

import com.gamesUP.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;

    /**
     * Convertit une entité Category en CategoryResponse (DTO)
     * Pattern: Data Transfer Object (DTO)
     * Principe SOLID: Single Responsibility Principle (SRP)
     * 
     * @param category L'entité Category à convertir
     * @return CategoryResponse ou null si l'entité est nulle
     */
    public static CategoryResponse fromEntity(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}