package com.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews") 
@Data
public class Avis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 1000)
    private String comment;
    
    @Column(nullable = false)
    private Integer rating; 
    
    private LocalDateTime createdDate;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
}
