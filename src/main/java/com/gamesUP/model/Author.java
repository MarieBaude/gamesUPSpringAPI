package com.gamesUP.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authors")
@Data
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String biography;
    
    @ManyToMany(mappedBy = "authors")
    @JsonIgnore  // ← AJOUTER : évite la boucle infinie
    private List<Game> games = new ArrayList<>();
}