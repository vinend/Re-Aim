package io.github.some_example_name.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a game map with associated song for the rhythm-based aim trainer.
 */
@Entity
@Table(name = "game_maps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    // Song details
    private String songTitle;
    private String artist;
    private String songFilename;
    
    // Path to the map file containing target spawn data
    private String mapDataFilename;
    
    // Each map can have a background image
    private String backgroundImagePath;
    
    // Map difficulty rating
    private int difficultyRating; // 1-10 scale
    
    // BPM of the song
    private int bpm;
    
    // Duration in seconds
    private int duration;
    
    // Whether the map is official or community-created
    private boolean official = true;
    
    // Map metadata for filtering/searching
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "map_tags", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    // One map has many scores
    @OneToMany(mappedBy = "map", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Score> scores = new HashSet<>();
}
