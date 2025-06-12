package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label; 
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane; 
import com.badlogic.gdx.scenes.scene2d.ui.Skin; 
import com.badlogic.gdx.scenes.scene2d.ui.Table; 
import com.badlogic.gdx.scenes.scene2d.ui.TextButton; 
import com.badlogic.gdx.scenes.scene2d.ui.Image; 
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
import io.github.some_example_name.models.Player; // Frontend player model for current user

public class LeaderboardScreen implements Screen {
    private final Main game;
    private final Player currentPlayer; // Logged-in player (renamed for clarity)
    private final String levelId;
    private Stage stage;
    private Skin skin;
    private Table leaderboardTable;
    private Label statusLabel;

    // --- Classes to represent the JSON structure from the backend for leaderboard ---
    // Based on: [{"id":"...", "player":{"id":"...","username":"..."}, "level":{...}, "score":28400, ...}]
    
    public static class PlayerInfo { // For nested player object in leaderboard entry
        public String id;
        public String username;
        // Add other player fields if needed (email, password are not typically shown on leaderboards)
    }

    public static class LevelInfo { // For nested level object (though not directly used for display here)
        public String id;
        public String name;
        // Add other level fields if needed
    }

    public static class ScoreEntry { // Represents a single entry in the leaderboard
        public String id; // The score entry's own ID
        public PlayerInfo player; // Nested player information
        public LevelInfo level;   // Nested level information (might not be used for display if level is known)
        public int score;         // Direct integer score
        // public int playerLevel; // Can be added if needed
    }
    // --- End of JSON structure classes ---


    public LeaderboardScreen(Main game, Player player, String levelId) {
        this.game = game;
        this.currentPlayer = player;
        this.levelId = levelId;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("LeaderboardScreen", "Could not load skin 'ui/uiskin.json'", e);
            skin = new Skin(); 
        }

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top().pad(20);
        stage.addActor(mainTable);

        // Get level name for the title
        io.github.some_example_name.managers.LevelManager levelManager = io.github.some_example_name.managers.LevelManager.getInstance();
        String levelDisplayName = levelId;
        if (levelManager.getLevel(levelId) != null) {
            levelDisplayName = levelManager.getLevel(levelId).getName();
        }
        
        Label titleLabel = new Label("Leaderboard: " + levelDisplayName, skin); 
        titleLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE); 
        mainTable.add(titleLabel).padBottom(20).colspan(3).row(); // Colspan 3 for Rank, Player, Score

        statusLabel = new Label("Loading leaderboard...", skin);
        mainTable.add(statusLabel).padBottom(20).colspan(3).row();
        
        leaderboardTable = new Table(skin);
        ScrollPane scrollPane = new ScrollPane(leaderboardTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); 

        mainTable.add(scrollPane).growX().growY().padBottom(20).colspan(3).row();

        TextButton backButton = new TextButton("Back to Level Select", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game, currentPlayer));
            }
        });
        mainTable.add(backButton).width(300).colspan(3).padTop(10);

        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.GET);
        httpRequest.setUrl("http://localhost:3000/api/scores/level/" + levelId); 
        httpRequest.setHeader("Content-Type", "application/json");

        Gdx.app.log("LeaderboardScreen", "Fetching leaderboard from: " + httpRequest.getUrl());

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseString = httpResponse.getResultAsString();
                Gdx.app.log("LeaderboardScreen", "Server responded with status: " + statusCode);
                // Gdx.app.log("LeaderboardScreen", "Response: " + responseString); // Log if needed for debugging

                if (statusCode == 200) {
                    try {
                        Json json = new Json();
                        json.setIgnoreUnknownFields(true); // Important for flexibility
                        Array<ScoreEntry> scores = json.fromJson(Array.class, ScoreEntry.class, responseString);
                        
                        if (scores != null && !scores.isEmpty()) {
                            statusLabel.setText("Top Scores:");
                            populateLeaderboardTable(scores);
                        } else {
                            statusLabel.setText("No scores found for this level.");
                        }
                    } catch (Exception e) {
                        Gdx.app.error("LeaderboardScreen", "Could not parse leaderboard data. Response was: " + responseString, e);
                        statusLabel.setText("Error parsing leaderboard data.");
                    }
                } else {
                    statusLabel.setText("Failed to load leaderboard. Status: " + statusCode);
                    Gdx.app.error("LeaderboardScreen", "Failed to load leaderboard. Response: " + responseString);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("LeaderboardScreen", "HTTP request failed!", t);
                statusLabel.setText("Error: Could not connect to server.");
            }

            @Override
            public void cancelled() {
                Gdx.app.error("LeaderboardScreen", "HTTP request was cancelled.");
                statusLabel.setText("Error: Request cancelled.");
            }
        });
    }

    private void populateLeaderboardTable(Array<ScoreEntry> scores) {
        leaderboardTable.clearChildren(); 
        leaderboardTable.top(); 

        // Add headers
        Label rankHeader = new Label("Rank", skin); 
        rankHeader.setColor(com.badlogic.gdx.graphics.Color.LIGHT_GRAY);
        leaderboardTable.add(rankHeader).pad(10).width(100).center(); // Fixed width for rank

        Label playerHeader = new Label("Player", skin); 
        playerHeader.setColor(com.badlogic.gdx.graphics.Color.LIGHT_GRAY);
        leaderboardTable.add(playerHeader).pad(10).expandX().left(); // Player name can expand

        Label scoreHeader = new Label("Score", skin); 
        scoreHeader.setColor(com.badlogic.gdx.graphics.Color.LIGHT_GRAY);
        leaderboardTable.add(scoreHeader).pad(10).width(150).right().row(); // Fixed width for score
        
        Image separatorImage = new Image(skin.newDrawable("white", com.badlogic.gdx.graphics.Color.DARK_GRAY)); 
        leaderboardTable.add(separatorImage).height(1).colspan(3).growX().padTop(2).padBottom(5).row();

        int rank = 1;
        for (ScoreEntry entry : scores) {
            String playerName = "Unknown";
            if (entry.player != null && entry.player.username != null) {
                playerName = entry.player.username;
            }
            
            leaderboardTable.add(new Label(String.valueOf(rank++), skin)).pad(5).center();
            leaderboardTable.add(new Label(playerName, skin)).pad(5).left();
            leaderboardTable.add(new Label(String.valueOf(entry.score), skin)).pad(5).right().row();
        }
        if (scores.isEmpty()) {
             leaderboardTable.add(new Label("No scores yet for this level!", skin)).colspan(3).center().pad(20);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
    }
}
