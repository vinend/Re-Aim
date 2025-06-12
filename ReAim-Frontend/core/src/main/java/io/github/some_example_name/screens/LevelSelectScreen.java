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

import java.util.Map;
// Removed HashMap import as levelDisplayToIdMap might not be needed in the same way.

public class LevelSelectScreen implements Screen {
    private final Main game;
    private final Player player; // Logged-in player
    private Stage stage;
    private Skin skin;
    private LevelManager levelManager;
    public Levels levelsData; // This holds the data read from the local file

    private Table levelsContainerTable; // Renamed from levelsTable to avoid confusion with main table
    private ScrollPane scrollPane;

    // --- Data Structures for building the SERVER-SIDE JSON payload (unchanged) ---
    public static class CreatorPayload {
        public String id;
    }
    public static class LevelPayload {
        public String _id;
        public String name;
        public String musicFileName;
        public String analysisFileName;
        public String difficulty;
        public CreatorPayload creator;
    }
    public static class LevelsPayload {
        public Array<LevelPayload> levels = new Array<>();
    }

    public LevelSelectScreen(Main game, Player player) {
        this.game = game;
        this.player = player;
        this.levelManager = LevelManager.getInstance();
        // this.levelDisplayToIdMap = new HashMap<>(); // May not be needed
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        loadLevelsAndPost(); // Keep this for server interaction

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("LevelSelectScreen", "Could not load skin 'ui/uiskin.json'", e);
            skin = new Skin(); // Minimal fallback
        }
        
        Table mainTable = new Table(); // This is the root table for the screen
        mainTable.setFillParent(true);
        mainTable.top().pad(20); // Align to top and add padding
        stage.addActor(mainTable);

        Label titleLabel = new Label("Select Level", skin); // Reverted to default font style
        // If you want a specific color, it can be set separately: titleLabel.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        // Or ensure "font-subtitle" with a color is defined in uiskin.json
        mainTable.add(titleLabel).padBottom(20).colspan(1).row(); // Colspan 1 as it's a single column layout now

        levelsContainerTable = new Table(skin); // Table to hold each level's row
        scrollPane = new ScrollPane(levelsContainerTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal, enable vertical

        // Add the scrollPane containing the levels list to the main table
        // Make it expand and fill available space
        mainTable.add(scrollPane).expand().fill().padBottom(20).row();

        populateLevelsList(); // New method to create the list of levels with buttons

        // "View My Scores" Button
        if (player != null && player.getId() != null) { // Only show if player is logged in
            TextButton viewMyScoresButton = new TextButton("View My Scores", skin);
            viewMyScoresButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setScreen(new PlayerScoresScreen(game, player));
                }
            });
            mainTable.add(viewMyScoresButton).width(300).padTop(10).row();
        }

        TextButton backButton = new TextButton("Back to Main Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game)); 
            }
        });
        // Add back button at the bottom of the main table
        mainTable.add(backButton).width(300).padTop(10).row();
    }

    private void populateLevelsList() {
        levelsContainerTable.clearChildren(); // Clear previous entries if any
        levelsContainerTable.top(); // Align items to the top of this inner table

        Map<String, LevelManager.LevelData> allLevels = levelManager.getAllLevels();
        if (allLevels.isEmpty()) {
            Gdx.app.log("LevelSelectScreen", "No levels found by LevelManager.");
            levelsContainerTable.add(new Label("No Levels Available", skin)).pad(20);
            return;
        }

        for (LevelManager.LevelData levelData : allLevels.values()) {
            String displayName = levelData.getName() + " (" + levelData.getDifficulty() + ")";
            final String currentLevelId = levelData.getId(); // Final for use in listener

            Label levelLabel = new Label(displayName, skin, "default"); // Assuming "font-list" might also be an issue, fallback to "default"
            // If "font-list" is valid and only "font-subtitle" was the problem, this could be "font-list"
            // For safety, using "default". Color can be set via levelLabel.setColor(...) if needed.
            
            TextButton playButton = new TextButton("Play", skin);
            playButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.log("LevelSelectScreen", "Starting game with level: " + currentLevelId);
                    game.setScreen(new GameScreen(game, player, currentLevelId));
                }
            });

            TextButton leaderboardButton = new TextButton("Leaderboard", skin);
            leaderboardButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Gdx.app.log("LevelSelectScreen", "Viewing leaderboard for level: " + currentLevelId);
                    game.setScreen(new LeaderboardScreen(game, player, currentLevelId));
                }
            });

            // Add to the levelsContainerTable
            levelsContainerTable.add(levelLabel).left().padRight(20).expandX();
            levelsContainerTable.add(playButton).width(100).padRight(10);
            levelsContainerTable.add(leaderboardButton).width(150);
            levelsContainerTable.row().padTop(10).padBottom(10); // Add some spacing between rows
        }
    }
    
    // loadLevelsAndPost and postLevelsToServer methods remain unchanged as per instructions
    // They handle backend communication and are not directly tied to the SelectBox UI that was removed.

    /**
     * Loads levels.json, parses it, and then posts the data to a server.
     */
    private void loadLevelsAndPost() {
        try {
            FileHandle fileHandle = Gdx.files.internal("MUSIC/data/levels.json");
            if (!fileHandle.exists()) {
                 // Try the alternative path if the primary one doesn't exist
                fileHandle = Gdx.files.internal("assets/MUSIC/levels/levels.json");
                if (!fileHandle.exists()) {
                    Gdx.app.error("Main", "levels.json not found at MUSIC/data/levels.json or assets/MUSIC/levels/levels.json");
                    return;
                }
            }
            String jsonString = fileHandle.readString();
            String modifiedJsonString = jsonString.replaceAll("\"id\":", "\"_id\":");
            
            Json json = new Json();
            levelsData = json.fromJson(Levels.class, modifiedJsonString);
            
            if (levelsData != null && levelsData.levels != null) {
                Gdx.app.log("Main", "Successfully loaded " + levelsData.levels.size + " levels from local file.");
                if (player != null && player.getId() != null) { // Only post if player is logged in
                    postLevelsToServer();
                } else {
                    Gdx.app.log("Main", "Player not logged in or player ID is null, skipping postLevelsToServer.");
                }
            } else {
                 Gdx.app.error("Main", "Failed to parse levels.json after loading.");
            }
        } catch (Exception e) {
            Gdx.app.error("Main", "Could not load or parse levels.json", e);
        }
    }

    /**
     * Sends the loaded level data to a server endpoint via HTTP POST.
     */
    private void postLevelsToServer() {
        if (levelsData == null || levelsData.levels == null || levelsData.levels.isEmpty()) {
            Gdx.app.log("Main_WebServer", "No local level data to post.");
            return;
        }
        if (player == null || player.getId() == null) {
            Gdx.app.log("Main_WebServer", "Player not available for posting levels.");
            return; // Cannot post without a creator
        }


        CreatorPayload levelCreator = new CreatorPayload();
        levelCreator.id = this.player.getId();

        LevelsPayload payload = new LevelsPayload();
        for (Main.Level levelFromFile : levelsData.levels) {
            LevelPayload levelForServer = new LevelPayload();
            levelForServer._id = levelFromFile._id; // Ensure this matches your Main.Level structure
            levelForServer.name = levelFromFile.name;
            levelForServer.musicFileName = levelFromFile.musicFileName;
            levelForServer.analysisFileName = levelFromFile.analysisFileName;
            levelForServer.difficulty = levelFromFile.difficulty;
            levelForServer.creator = levelCreator; 
            
            payload.levels.add(levelForServer);
        }

        Json json = new Json(JsonWriter.OutputType.json);
        String jsonDataString = json.toJson(payload);

        Net.HttpRequest httpRequest = new Net.HttpRequest(Net.HttpMethods.POST);
        httpRequest.setUrl("http://localhost:3000/api/levels/create"); // Ensure this URL is correct
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setContent(jsonDataString);

        Gdx.app.log("Main_WebServer", "Posting level data to " + httpRequest.getUrl());
        // Gdx.app.log("Main_WebServer", "Request Body: " + jsonDataString); // Potentially verbose

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("Main_WebServer", "Server responded with status: " + httpResponse.getStatus().getStatusCode());
                // Gdx.app.log("Main_WebServer", "Response: " + httpResponse.getResultAsString()); // Potentially verbose
                // The LevelManager's syncLevelsWithBackend or initial load should handle fetching/updating.
                // We can re-populate the list here to reflect any changes.
                // However, LevelManager.fetchLevelsFromServer with a callback isn't defined.
                // The LevelManager's constructor calls loadLevels() and syncLevelsWithBackend().
                // The populateLevelsList() in show() should use the levels available in LevelManager.
                // If levels are dynamically added/updated on the server and we need to refresh,
                // LevelManager would need a method to re-fetch and a callback.
                // For now, we assume initial load + sync is sufficient, or a manual refresh mechanism elsewhere.
                // If postLevelsToServer is successful, the LevelManager might already be up-to-date
                // or a separate call to fetch/refresh levels in LevelManager would be needed.
                // For simplicity, let's assume LevelManager handles its state.
                // We can call populateLevelsList again if needed, ensuring it's on the GDX thread.
                if (stage != null && levelsContainerTable != null) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.app.log("LevelSelectScreen", "Attempting to re-populate levels list after server post response.");
                            populateLevelsList(); // Re-draw the list based on current LevelManager state
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Main_WebServer", "HTTP request to post levels failed!", t);
            }

            @Override
            public void cancelled() {
                Gdx.app.error("Main_WebServer", "HTTP request to post levels was cancelled.");
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
        // Skin is typically managed centrally
    }
}
