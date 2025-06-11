package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
import io.github.some_example_name.screens.LoginRegisterScreen;

public class MainMenuScreen implements Screen {

    private Main game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Main game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin (uiskin.json is expected to be in assets/ui)
        // Ensure you have a uiskin.json, uiskin.atlas, and uiskin.png in your assets/ui folder
        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading skin: ui/uiskin.json", e);
            // Fallback to a default skin or handle error appropriately
            // For now, we'll let it potentially crash if skin is essential and missing
            // or create a very basic skin programmatically if needed.
            // This example assumes uiskin.json is correctly set up.
            skin = new Skin(); // Minimal fallback
        }


        Table table = new Table();
        table.setFillParent(true);
        table.center();

        TextButton startButton = new TextButton("START", skin);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dispose();
                game.setScreen(new LoginRegisterScreen(game));
            }
        });

        table.add(startButton).width(300).height(100).pad(10);
        table.row();
        // Add more buttons like Options, Exit if needed

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
    }
}
