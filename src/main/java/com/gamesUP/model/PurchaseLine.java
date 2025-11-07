package com.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "purchase_lines")
@Data
public class PurchaseLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double unitPrice;
    
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;
}
