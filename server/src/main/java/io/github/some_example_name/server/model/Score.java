package io.github.some_example_name.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a player score in the ReAim game.
 */
@Entity
@Table(name = "scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "map_id", nullable = false)
    private GameMap map;
    
    private int score;
    private int hits;
    private int misses;
    private int maxCombo;
    private float accuracy;
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    // Pre-persist hook to automatically set timestamp
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    public enum Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }
}
