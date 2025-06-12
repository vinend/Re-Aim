package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
import io.github.some_example_name.models.Player; // Frontend player model for current user
import io.github.some_example_name.managers.LevelManager; // To get level names if needed, though backend provides name

public class PlayerScoresScreen implements Screen {
    private final Main game;
    private final Player currentPlayer; // Logged-in player
    private Stage stage;
    private Skin skin;
    private Table scoresTable;
    private Label statusLabel;
    private LevelManager levelManager; // Still useful if we want to cross-reference or for consistency

    // --- Classes to represent the JSON structure from the backend for player scores ---
    // Based on: [{"id":"...", "player":{"id":"...","username":"..."}, "level":{"id":"...","name":"..."}, "score":27800, ...}]
    
    public static class PlayerInfo { // For nested player object
        public String id;
        public String username;
        // email, password typically not needed for display
    }

    public static class LevelInfo { // For nested level object
        public String id;
        public String name;
        // jsonFile, difficulty, creator might not be needed for this specific display
    }
    
    public static class PlayerScoreEntry {
        public String id; // The score entry's own ID
        public PlayerInfo player; 
        public LevelInfo level;   
        public int score;         // Direct integer score
        // public int playerLevel; // Can be added if needed
    }
    // --- End of JSON structure classes ---

    public PlayerScoresScreen(Main game, Player player) {
        this.game = game;
        this.currentPlayer = player;
        this.levelManager = LevelManager.getInstance();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("PlayerScoresScreen", "Could not load skin 'ui/uiskin.json'", e);
            skin = new Skin(); 
        }

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top().pad(20);
        stage.addActor(mainTable);

        String playerNameDisplay = (currentPlayer != null && currentPlayer.getUsername() != null) ? currentPlayer.getUsername() : "Player";
        Label titleLabel = new Label(playerNameDisplay + "'s Scores", skin);
        titleLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        mainTable.add(titleLabel).padBottom(20).colspan(2).row(); // Colspan 2 for Level, Score

        statusLabel = new Label("Loading scores...", skin);
        mainTable.add(statusLabel).padBottom(20).colspan(2).row();
        
        scoresTable = new Table(skin);
        ScrollPane scrollPane = new ScrollPane(scoresTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).growX().growY().padBottom(20).colspan(2).row();

        TextButton backButton = new TextButton("Back to Level Select", skin); 
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game, currentPlayer)); 
            }
        });
        mainTable.add(backButton).width(300).colspan(2).padTop(10);

        if (currentPlayer != null && currentPlayer.getId() != null) {
            fetchPlayerScores(currentPlayer.getId());
        } else {
            statusLabel.setText("Cannot load scores: Player not logged in.");
            Gdx.app.error("PlayerScoresScreen", "Player or Player ID is null.");
        }
    }

    private void fetchPlayerScores(String playerId) {
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.GET);
        httpRequest.setUrl("http://localhost:3000/api/scores/player/" + playerId); 
        httpRequest.setHeader("Content-Type", "application/json");

        Gdx.app.log("PlayerScoresScreen", "Fetching scores for player ID: " + playerId + " from " + httpRequest.getUrl());

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseString = httpResponse.getResultAsString();
                Gdx.app.log("PlayerScoresScreen", "Server responded with status: " + statusCode);
                // Gdx.app.log("PlayerScoresScreen", "Response: " + responseString); 

                if (statusCode == 200) {
                    try {
                        Json json = new Json();
                        json.setIgnoreUnknownFields(true); 
                        Array<PlayerScoreEntry> scores = json.fromJson(Array.class, PlayerScoreEntry.class, responseString);
                        
                        if (scores != null && !scores.isEmpty()) {
                            statusLabel.setText("Your Scores:");
                            populateScoresTable(scores);
                        } else {
                            statusLabel.setText("No scores found for this player.");
                        }
                    } catch (Exception e) {
                        Gdx.app.error("PlayerScoresScreen", "Could not parse player scores data. Response was: " + responseString, e);
                        statusLabel.setText("Error parsing scores data.");
                    }
                } else {
                    statusLabel.setText("Failed to load scores. Status: " + statusCode);
                     Gdx.app.error("PlayerScoresScreen", "Failed to load scores. Response: " + responseString);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("PlayerScoresScreen", "HTTP request failed!", t);
                statusLabel.setText("Error: Could not connect to server.");
            }

            @Override
            public void cancelled() {
                Gdx.app.error("PlayerScoresScreen", "HTTP request was cancelled.");
                statusLabel.setText("Error: Request cancelled.");
            }
        });
    }

    private void populateScoresTable(Array<PlayerScoreEntry> scores) {
        scoresTable.clearChildren(); 
        scoresTable.top();

        // Add headers (Level Name, Score)
        Label levelHeader = new Label("Level Name", skin); 
        levelHeader.setColor(com.badlogic.gdx.graphics.Color.LIGHT_GRAY);
        scoresTable.add(levelHeader).pad(10).expandX().left();

        Label scoreHeader = new Label("Score", skin); 
        scoreHeader.setColor(com.badlogic.gdx.graphics.Color.LIGHT_GRAY);
        scoresTable.add(scoreHeader).pad(10).width(150).right(); 
        scoresTable.row();
        
        Image separatorImage = new Image(skin.newDrawable("white", com.badlogic.gdx.graphics.Color.DARK_GRAY));
        scoresTable.add(separatorImage).height(1).colspan(2).growX().padTop(2).padBottom(5).row(); 

        for (PlayerScoreEntry entry : scores) {
            String levelName = "Unknown Level";
            if (entry.level != null && entry.level.name != null) {
                levelName = entry.level.name;
            } else if (entry.level != null && entry.level.id != null) {
                // Fallback to resolving via LevelManager if name is missing in response
                LevelManager.LevelData ld = levelManager.getLevel(entry.level.id);
                if (ld != null) levelName = ld.getName();
                else levelName = "Level ID: " + entry.level.id;
            }
            
            scoresTable.add(new Label(levelName, skin)).pad(5).expandX().left();
            scoresTable.add(new Label(String.valueOf(entry.score), skin)).pad(5).width(150).right(); 
            scoresTable.row();
        }
        if (scores.isEmpty()) {
             scoresTable.add(new Label("You have no scores yet!", skin)).colspan(2).center().pad(20); 
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
