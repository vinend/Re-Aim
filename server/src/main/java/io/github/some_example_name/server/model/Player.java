package io.github.some_example_name.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a player in the ReAim game.
 */
@Entity
@Table(name = "players",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    // Profile picture stored as a byte array
    @Lob
    @Column(name = "profile_picture")
    private byte[] profilePicture;
    
    // Track if the player has a custom profile picture
    @Column(name = "has_custom_profile")
    private boolean hasCustomProfile = false;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Score> scores = new HashSet<>();
    
    // Additional player stats
    private int gamesPlayed = 0;
    private int totalShots = 0;
    private int totalHits = 0;
    private long totalPlaytime = 0; // in seconds

    public float getAccuracy() {
        return totalShots > 0 ? (float) totalHits / totalShots * 100 : 0;
    }
}
