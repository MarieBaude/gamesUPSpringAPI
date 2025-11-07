package com.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publishers")
@Data
public class Publisher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String contactInfo;
    
    @OneToMany(mappedBy = "publisher")
    private List<Game> games = new ArrayList<>();
}
