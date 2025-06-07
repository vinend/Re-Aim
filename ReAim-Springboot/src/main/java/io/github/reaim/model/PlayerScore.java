package io.github.reaim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.Min;

@Document(collection = "player_scores")
public class PlayerScore {
    @Id
    private String id;
    
    @DBRef
    private Player player;
    
    @DBRef
    private Level level;
    
    @Min(0)
    private int score;

    @Min(0)
    private int playerLevel;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getPlayerLevel() { return playerLevel; }
    public void setPlayerLevel(int playerLevel) { this.playerLevel = playerLevel; }
}
