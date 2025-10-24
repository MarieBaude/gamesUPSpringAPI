package com.gamesUP.gamesUP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Data
public class Purchase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime purchaseDate;
    
    private Double totalAmount;
    
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL)
    private List<PurchaseLine> purchaseLines = new ArrayList<>();
}