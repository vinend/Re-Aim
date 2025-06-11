package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Main;
import io.github.some_example_name.screens.GameScreen;
import io.github.some_example_name.screens.MainMenuScreen;
import io.github.some_example_name.models.Player;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class LoginRegisterScreen implements Screen {
    private final Main game;
    private final Stage stage;
    private final Skin skin;
    private final static String API_URL = "http://localhost:3000/api/auth";

    public LoginRegisterScreen(Main game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Login Section
        Label loginLabel = new Label("Login", skin);
        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        TextField passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);
        TextButton loginButton = new TextButton("Login", skin);

        // Register Section
        Label registerLabel = new Label("Register", skin);
        TextField regUsernameField = new TextField("", skin);
        regUsernameField.setMessageText("Username");
        TextField regEmailField = new TextField("", skin);
        regEmailField.setMessageText("Email");
        TextField regPasswordField = new TextField("", skin);
        regPasswordField.setMessageText("Password");
        regPasswordField.setPasswordCharacter('*');
        regPasswordField.setPasswordMode(true);
        TextButton registerButton = new TextButton("Register", skin);
        TextButton backButton = new TextButton("Back to Main Menu", skin);

        // Layout
        float width = 200f;
        float pad = 10f;

        table.add(loginLabel).colspan(2).pad(pad);
        table.row();
        table.add(usernameField).width(width).pad(pad);
        table.row();
        table.add(passwordField).width(width).pad(pad);
        table.row();
        table.add(loginButton).width(width).pad(pad);
        table.row().padTop(30f);
        
        table.add(registerLabel).colspan(2).pad(pad);
        table.row();
        table.add(regUsernameField).width(width).pad(pad);
        table.row();
        table.add(regEmailField).width(width).pad(pad);
        table.row();
        table.add(regPasswordField).width(width).pad(pad);
        table.row();
        table.add(registerButton).width(width).pad(pad);
        table.row().padTop(30f);
        table.add(backButton).width(width).pad(pad);

        // Button Listeners
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                if (username.isEmpty() || password.isEmpty()) {
                    showError("Please fill in all fields");
                    return;
                }
                login(username, password);
            }
        });

        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String username = regUsernameField.getText();
                String email = regEmailField.getText();
                String password = regPasswordField.getText();
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showError("Please fill in all fields");
                    return;
                }
                register(username, email, password);
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dispose();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    private void showError(String message) {
        Dialog dialog = new Dialog("Error", skin);
        dialog.text(message);
        dialog.button("OK");
        dialog.show(stage);
    }

    private void login(String username, String password) {
        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(API_URL + "/login")
            .header("Content-Type", "application/json")
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String response = httpResponse.getResultAsString();
                Gdx.app.log("Login Response", "Status Code: " + statusCode + ", Raw response: " + response);

                if (statusCode >= 200 && statusCode < 300) { // Success (2xx)
                    // Try direct access to test response structure
                    Gdx.app.log("Login Debug", "Attempting to parse response...");
                    try {
                        JsonReader jsonReader = new JsonReader();
                        JsonValue json = jsonReader.parse(response);
                        
                        Gdx.app.log("Login JSON", "Parsing response: " + json.toString());
                        
                        JsonValue playerJson = json.get("player");
                        if (playerJson == null) {
                            Gdx.app.error("Login", "No player object in response");
                            throw new RuntimeException("Player data not found");
                        }
                        
                        String id = playerJson.getString("id");
                        String email = playerJson.getString("email");
                        
                        Gdx.app.log("Player Data", "Creating player with ID: " + id);
                        Player player = new Player(id, username, email);
                        
                        Gdx.app.log("Player Data", "ID: " + id + ", Username: " + username + ", Email: " + email);
                        Gdx.app.log("Login Success", "Message: Success");
                        Gdx.app.log("Login", "Player logged in - ID: " + id + ", Username: " + username);
                        
                        Gdx.app.postRunnable(() -> {
                            try {
                                // Create and transition to game screen in the same thread
                                GameScreen gameScreen = new GameScreen(game, player);
                                dispose(); // Clean up current screen
                                game.setScreen(gameScreen); // Switch to game screen
                                Gdx.app.log("Login", "Screen transition complete");
                            } catch (Exception e) {
                                Gdx.app.error("Login", "Error during screen transition: " + e.getMessage(), e);
                                showError("Error starting game");
                            }
                        });
                    } catch (Exception e) {
                        Gdx.app.error("Login", "Error parsing response: " + e.getMessage(), e);
                        showError("Error processing login response");
                    }
                } else {
                    Gdx.app.postRunnable(() -> {
                        showError("Login failed: " + response);
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Login Error", t.getMessage(), t);
                Gdx.app.postRunnable(() -> {
                    showError("Network error: " + t.getMessage());
                });
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private void register(String username, String email, String password) {
        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(API_URL + "/register")
            .header("Content-Type", "application/json")
            .content("{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String status = httpResponse.getStatus().toString();
                String response = httpResponse.getResultAsString();
                Gdx.app.log("Register Response", "Status: " + status + ", Content: " + response);

                if (status.startsWith("2")) { // Success (2xx)
                    Gdx.app.postRunnable(() -> {
                        Dialog dialog = new Dialog("Success", skin);
                        dialog.text("Registration successful! Please login.");
                        dialog.button("OK");
                        dialog.show(stage);
                    });
                } else {
                    Gdx.app.postRunnable(() -> {
                        showError("Registration failed: " + response);
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Register Error", t.getMessage(), t);
                Gdx.app.postRunnable(() -> {
                    showError("Network error: " + t.getMessage());
                });
            }

            @Override
            public void cancelled() {
            }
        });
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
        skin.dispose();
    }
}
