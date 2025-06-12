package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
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

    public LevelSelectScreen(Main game, Player player) {
        this.game = game;
        this.player = player;
        this.levelManager = LevelManager.getInstance(); // Use singleton instance
        this.levelDisplayToIdMap = new HashMap<>();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // Ensure uiskin.json is in assets/ui
        } catch (Exception e) {
            Gdx.app.error("LevelSelectScreen", "Could not load skin 'ui/uiskin.json'", e);
            // Fallback to a default skin or handle error
            skin = new Skin(); // Minimal fallback
        }
        
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        Label titleLabel = new Label("Select Level", skin); // Use default label style
        table.add(titleLabel).padBottom(40).colspan(2).row();

        levelSelectBox = new SelectBox<>(skin);
        populateLevelSelectBox();
        if (!levelDisplayToIdMap.isEmpty() && levelSelectBox.getItems().size > 0) {
            // Set default selection if possible
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
                    // Optionally, show a message to the user
                }
            }
        });
        table.add(playButton).colspan(2).padTop(20).width(300).row();

        TextButton backButton = new TextButton("Back to Main Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Assuming MainMenuScreen is pre-login and doesn't need player object
                game.setScreen(new MainMenuScreen(game)); 
            }
        });
        table.add(backButton).colspan(2).padTop(10).width(300);
    }

    private void populateLevelSelectBox() {
        Map<String, LevelManager.LevelData> allLevels = levelManager.getAllLevels();
        if (allLevels.isEmpty()) {
            Gdx.app.log("LevelSelectScreen", "No levels found by LevelManager.");
            levelSelectBox.setItems("No Levels Available"); // Placeholder
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
    public void pause() {
    }

    @Override
    public void resume() {
    }

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
            // Only dispose if this screen loaded it exclusively and it's not managed globally
            // skin.dispose(); 
            // For uiskin.json, it's often loaded once by GameAssets or Main.
            // If LevelSelectScreen loads it directly and exclusively, then dispose.
            // For now, assuming it might be shared or to prevent crash if already disposed.
        }
    }
}
