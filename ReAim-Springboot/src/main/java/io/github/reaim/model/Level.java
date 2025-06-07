package io.github.reaim.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "levels")
public class Level {
    @Id
    private String id;

    @NotBlank
    private String name;

    private String jsonFile;

    @NotBlank
    private String difficulty;

    @DBRef
    private Player creator;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getJsonFile() { return jsonFile; }
    public void setJsonFile(String jsonFile) { this.jsonFile = jsonFile; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Player getCreator() { return creator; }
    public void setCreator(Player creator) { this.creator = creator; }
}
