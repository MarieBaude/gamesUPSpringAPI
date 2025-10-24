package com.gamesUP.gamesUP.model;

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
    private List<Game> games = new ArrayList<>();
}
