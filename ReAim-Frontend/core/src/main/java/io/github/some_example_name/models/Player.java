package io.github.some_example_name.models;

public class Player {
    private String id;
    private String username;
    private String email;

    public Player(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}