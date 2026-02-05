package com.gamesUP.dto.response;

import com.gamesUP.model.Publisher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherResponse {
    private Long id;
    private String name;
    private String contactInfo;

    /**
     * Convertit une entité Publisher en PublisherResponse (DTO)
     * Pattern: Data Transfer Object (DTO)
     * Principe SOLID: Single Responsibility Principle (SRP)
     * 
     * @param publisher L'entité Publisher à convertir
     * @return PublisherResponse ou null si l'entité est nulle
     */
    public static PublisherResponse fromEntity(Publisher publisher) {
        if (publisher == null) {
            return null;
        }

        return PublisherResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .contactInfo(publisher.getContactInfo())
                .build();
    }
}