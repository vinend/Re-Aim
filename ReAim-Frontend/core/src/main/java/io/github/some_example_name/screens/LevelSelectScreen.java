package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
import io.github.some_example_name.Main.Levels; // Imports the structure for reading the local file
import io.github.some_example_name.managers.LevelManager;
import io.github.some_example_name.models.Player; // Frontend Player model

import java.util.HashMap;
import java.util.Map;


public class LevelSelectScreen implements Screen {
    private final Main game;
    private final Player player; // Logged-in player
    private Stage stage;
    private Skin skin;
    private LevelManager levelManager;

    private SelectBox<String> levelSelectBox;
    private Map<String, String> levelDisplayToIdMap; // Maps display name to actual level ID
    private String selectedLevelId;
    public Levels levelsData; // This holds the data read from the local file


    // --- Data Structures for building the SERVER-SIDE JSON payload ---
    // These classes define the exact structure the server expects.

    /** Represents the creator of a level, containing just the player's ID. */
    public static class CreatorPayload {
        public String id;
    }

    /** Represents a single level in the format required by the server. */
    public static class LevelPayload {
        public String _id;
        public String name;
        public String musicFileName;
        public String analysisFileName;
        public String difficulty;
        public CreatorPayload creator; // The required creator object
    }

    /** Represents the root object for the server-side payload. */
    public static class LevelsPayload {
        public Array<LevelPayload> levels = new Array<>();
    }


    public LevelSelectScreen(Main game, Player player) {
        this.game = game;
        this.player = player;
        this.levelManager = LevelManager.getInstance();
        this.levelDisplayToIdMap = new HashMap<>();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        // This will now load the local file and immediately post the corrected data
        loadLevelsAndPost();

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("LevelSelectScreen", "Could not load skin 'ui/uiskin.json'", e);
            skin = new Skin(); // Minimal fallback
        }
        
        // --- UI Setup (unchanged) ---
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Label titleLabel = new Label("Select Level", skin);
        table.add(titleLabel).padBottom(40).colspan(2).row();

        levelSelectBox = new SelectBox<>(skin);
        populateLevelSelectBox();
        if (!levelDisplayToIdMap.isEmpty() && levelSelectBox.getItems().size > 0) {
            String defaultDisplayName = levelSelectBox.getItems().first();
            levelSelectBox.setSelected(defaultDisplayName);
            selectedLevelId = levelDisplayToIdMap.get(defaultDisplayName);
        }

        levelSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDisplayName = levelSelectBox.getSelected();
                selectedLevelId = levelDisplayToIdMap.get(selectedDisplayName);
                Gdx.app.log("LevelSelectScreen", "Selected Level ID: " + selectedLevelId);
            }
        });
        table.add(new Label("Choose a song:", skin)).padRight(10);
        table.add(levelSelectBox).width(300).padBottom(20).row();


        TextButton playButton = new TextButton("Play Selected Level", skin);
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedLevelId != null && !selectedLevelId.isEmpty()) {
                    Gdx.app.log("LevelSelectScreen", "Starting game with level: " + selectedLevelId);
                    game.setScreen(new GameScreen(game, player, selectedLevelId));
                } else {
                    Gdx.app.log("LevelSelectScreen", "No level selected.");
                }
            }
        });
        table.add(playButton).colspan(2).padTop(20).width(300).row();

        TextButton backButton = new TextButton("Back to Main Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game)); 
            }
        });
        table.add(backButton).colspan(2).padTop(10).width(300);
    }

    private void populateLevelSelectBox() {
        Map<String, LevelManager.LevelData> allLevels = levelManager.getAllLevels();
        if (allLevels.isEmpty()) {
            Gdx.app.log("LevelSelectScreen", "No levels found by LevelManager.");
            levelSelectBox.setItems("No Levels Available");
            return;
        }

        Array<String> displayNames = new Array<>();
        for (LevelManager.LevelData levelData : allLevels.values()) {
            String displayName = levelData.getName() + " (" + levelData.getDifficulty() + ")";
            displayNames.add(displayName);
            levelDisplayToIdMap.put(displayName, levelData.getId());
        }
        levelSelectBox.setItems(displayNames);
    }

    /**
     * Loads levels.json, parses it, and then posts the data to a server.
     */
    private void loadLevelsAndPost() {
        try {
            FileHandle fileHandle = Gdx.files.internal("MUSIC/data/levels.json");
            String jsonString = fileHandle.readString();
            String modifiedJsonString = jsonString.replaceAll("\"id\":", "\"_id\":");
            
            Json json = new Json();
            // Read the local file using the structure defined in Main.java
            levelsData = json.fromJson(Levels.class, modifiedJsonString);
            
            if (levelsData != null && levelsData.levels != null) {
                Gdx.app.log("Main", "Successfully loaded " + levelsData.levels.size + " levels.");
                // Post the loaded data to the server using the correct format
                postLevelsToServer();
            } else {
                 Gdx.app.error("Main", "Failed to parse levels.json after loading.");
            }
        } catch (Exception e) {
            Gdx.app.error("Main", "Could not load or parse MUSIC/data/levels.json", e);
        }
    }

    /**
     * Sends the loaded level data to a server endpoint via HTTP POST.
     */
    private void postLevelsToServer() {
        if (levelsData == null || levelsData.levels == null || levelsData.levels.isEmpty()) {
            Gdx.app.log("Main_WebServer", "No level data to post.");
            return;
        }

        // 1. Create a creator object from the logged-in player.
        // This assumes your Player model has a getId() method.
        CreatorPayload levelCreator = new CreatorPayload();
        levelCreator.id = this.player.getId();

        // 2. Build the new payload object that matches the server's expectations.
        LevelsPayload payload = new LevelsPayload();
        for (Main.Level levelFromFile : levelsData.levels) {
            LevelPayload levelForServer = new LevelPayload();
            levelForServer._id = levelFromFile._id;
            levelForServer.name = levelFromFile.name;
            levelForServer.musicFileName = levelFromFile.musicFileName;
            levelForServer.analysisFileName = levelFromFile.analysisFileName;
            levelForServer.difficulty = levelFromFile.difficulty;
            levelForServer.creator = levelCreator; // Attach the creator to each level.
            
            payload.levels.add(levelForServer);
        }

        // 3. Convert the new payload object to a JSON string.
        Json json = new Json(JsonWriter.OutputType.json);
        String jsonDataString = json.toJson(payload);

        // 4. Send the request
        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl("http://localhost:3000/api/levels/create");
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setContent(jsonDataString);

        Gdx.app.log("Main_WebServer", "Posting level data to " + httpRequest.getUrl());
        Gdx.app.log("Main_WebServer", "Request Body: " + jsonDataString);

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("Main_WebServer", "Server responded with status: " + httpResponse.getStatus().getStatusCode());
                Gdx.app.log("Main_WebServer", "Response: " + httpResponse.getResultAsString());
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Main_WebServer", "HTTP request failed!", t);
            }

            @Override
            public void cancelled() {
                Gdx.app.error("Main_WebServer", "HTTP request was cancelled.");
            }
        });
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
        if (skin != null) {
            // Skin should typically be disposed by a central asset manager
            // to avoid disposing it while it's still in use by another screen.
            // skin.dispose();
        }
    }
}
