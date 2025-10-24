package com.gamesUP.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer quantity;
    
    private LocalDateTime lastRestock;
    
    @OneToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
}