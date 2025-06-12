package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Input; // For key codes
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.Main;
import io.github.some_example_name.assets.GameAssets;
import io.github.some_example_name.models.*; // ScoreEffect will be covered by this wildcard
import io.github.some_example_name.managers.LevelManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator; // For safely removing elements from list while iterating

public class GameScreen implements Screen {
    private final Main game;
    private final Player player;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GameAssets gameAssets;

    // Game objects
    private Gun gun;
    private Crosshair crosshair;
    private Array<Bullet> bulletCasings;
    private List<Target> activeTargets;
    private List<ScoreEffect> activeScoreEffects; // Added for score effects
    private LevelManager levelManager;

    // Gun movement and rendering properties
    private Vector2 gunPosition;
    private float gunLagFactor = 0.1f;
    private float gunOffsetX = 400f;
    private float fixedGunY = -280f;
    private float maxTiltAngle = -30f;
    private float gunScale = 0.8f;
    private TextureRegion staticGunTextureRegion;

    // Background textures
    private Texture bar1Texture;
    private Texture bar2Texture;

    // Sound effects
    private Sound gunshotSound;
    private Sound gunpingSound;
    private int shotCounter = 0;
    private int currentScore = 0;
    private boolean scoreSubmittedThisAttempt = false; // Flag to ensure score is submitted only once
    private String currentLevelId; // Store the ID of the level being played
    private InputAdapter gameInputAdapter; // Input processor for this screen
    private BitmapFont scoreFont;
    private GlyphLayout glyphLayout; // For text measurement

    private boolean isPaused = false;
    private TextureRegion pauseOverlayTexture; // For dimming the screen
    private boolean hasReturnedToLevelSelect = false; // Flag to prevent multiple transitions

    public GameScreen(Main game, Player player, String levelIdToPlay) {
        this.game = game;
        this.player = player;
        this.currentLevelId = levelIdToPlay; // Store the passed level ID
        
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        gameAssets = GameAssets.getInstance();
        scoreFont = gameAssets.getFont("font"); // Assuming GameAssets has a method to get a font
        glyphLayout = new GlyphLayout();

        // Create a 1x1 white pixel texture for the overlay
        Texture whitePixel = new Texture(Gdx.files.internal("libgdx.png")); // Placeholder, ideally a 1x1 white pixel
        // For a real 1x1 white pixel, you might create it programmatically if not available as an asset
        // Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        // pixmap.setColor(Color.WHITE);
        // pixmap.fill();
        // whitePixel = new Texture(pixmap);
        // pixmap.dispose();
        pauseOverlayTexture = new TextureRegion(whitePixel);


        // Hide the mouse cursor
        Gdx.input.setCursorCatched(true);

        // Initialize background textures
        bar1Texture = gameAssets.getBarTexture("bar1");
        bar2Texture = gameAssets.getBarTexture("bar2");

        // Load sound effects
        gunshotSound = Gdx.audio.newSound(Gdx.files.internal("GUN & BULLETS/GUNSHOOT.mp3"));
        gunpingSound = Gdx.audio.newSound(Gdx.files.internal("GUN & BULLETS/GUNPING.mp3"));

        // Initialize game objects
        crosshair = new Crosshair("3R");
        gun = new Gun(0, 0);
        gunPosition = new Vector2(Gdx.graphics.getWidth() / 2f, fixedGunY);
        gun.setPosition(gunPosition.x, gunPosition.y);

        bulletCasings = new Array<>();
        staticGunTextureRegion = new TextureRegion(gameAssets.getGunTexture("gub1"));
        
        // Initialize level system
        levelManager = LevelManager.getInstance(); // Use singleton instance
        activeTargets = new ArrayList<>();
        activeScoreEffects = new ArrayList<>(); // Initialize the list for score effects
        if (this.currentLevelId != null && !this.currentLevelId.isEmpty()) {
            levelManager.startLevel(this.currentLevelId);
        } else {
            Gdx.app.error("GameScreen", "No level ID provided, cannot start level.");
            // Fallback or error handling:
            // For now, let's try a default if available, or just log and the game might not function correctly.
            // A better approach would be to prevent GameScreen from being created without a valid level ID.
            // Or, have a default level ID if one must be played.
            // levelManager.startLevel("aint_that_a_kick_in_the_head"); // Example fallback
        }

        setupInputProcessor(); // Initialize the input adapter

        Gdx.app.log("GameScreen", "Started game for player: " + player.getUsername());
    }

    private void setupInputProcessor() {
        gameInputAdapter = new InputAdapter() { // Assign to the instance variable
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isPaused) {
                    // Handle pause menu clicks
                    // Convert screenY to world Y
                    float worldY = Gdx.graphics.getHeight() - screenY;
                    float menuCenterX = Gdx.graphics.getWidth() / 2f;
                    float resumeButtonY = Gdx.graphics.getHeight() / 2f + 25; // Approximate position
                    float quitButtonY = Gdx.graphics.getHeight() / 2f - 25;   // Approximate position
                    float buttonWidth = 200; // Approximate width
                    float buttonHeight = 50; // Approximate height

                    // Resume button
                    if (screenX >= menuCenterX - buttonWidth / 2 && screenX <= menuCenterX + buttonWidth / 2 &&
                        worldY >= resumeButtonY - buttonHeight / 2 && worldY <= resumeButtonY + buttonHeight / 2) {
                        resumeGame();
                        return true;
                    }

                    // Quit button
                    if (screenX >= menuCenterX - buttonWidth / 2 && screenX <= menuCenterX + buttonWidth / 2 &&
                        worldY >= quitButtonY - buttonHeight / 2 && worldY <= quitButtonY + buttonHeight / 2) {
                        quitToLevelSelect();
                        return true;
                    }
                    return true; // Consume click if paused
                }

                if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
                    gun.shoot();

                    if (gunshotSound != null) {
                        float volume = 0.15f;
                        float pitch = MathUtils.random(0.95f, 1.05f);
                        gunshotSound.play(volume, pitch, 0f);
                    }
                    shotCounter++;
                    if (gunpingSound != null && shotCounter % 8 == 0) {
                        float volume = 0.2f;
                        float pitch = MathUtils.random(0.98f, 1.02f);
                        gunpingSound.play(volume, pitch, 0f);
                    }

                    bulletCasings.add(new Bullet(gun.getX() - 1250f, gun.getY() + 20f));

                    float mouseX = crosshair.getCenterX();
                    float mouseY = crosshair.getCenterY();
                    for (Target target : activeTargets) {
                        if (!target.isDestroyed() && target.isHit(mouseX, mouseY)) {
                            target.destroy(); // Mark target as destroyed

                            // Determine score based on accuracy
                            float targetCenterX = target.getCenterX();
                            float targetCenterY = target.getCenterY();
                            float distance = Vector2.dst(mouseX, mouseY, targetCenterX, targetCenterY);

                            // Define score tiers based on distance from center
                            // Target.bounds.width and Target.bounds.height are already scaled
                            float maxDistanceForMaxScore = Math.min(target.getBounds().width, target.getBounds().height) * 0.15f; // e.g., 15% of smallest dimension for highest score
                            float maxDistanceForMidScore = Math.min(target.getBounds().width, target.getBounds().height) * 0.30f; // e.g., 30% for mid
                            float maxDistanceForLowScore = Math.min(target.getBounds().width, target.getBounds().height) * 0.50f; // e.g., 50% for low

                            int scoreValue;
                            if (distance <= maxDistanceForMaxScore) {
                                scoreValue = 1000;
                            } else if (distance <= maxDistanceForMidScore) {
                                scoreValue = 600;
                            } else if (distance <= maxDistanceForLowScore) {
                                scoreValue = 300;
                            } else {
                                scoreValue = 100; // Hit was on the edge
                            }
                            currentScore += scoreValue; // Add to total score

                            // Create score effect
                            String scoreTextureKey = "score" + scoreValue;
                            Texture scoreTexture = gameAssets.getScoreTexture(scoreTextureKey);
                            if (scoreTexture != null) {
                                // Use existing getCenterX() and getCenterY() from Target model
                                // float targetCenterX = target.getCenterX(); // Already defined above
                                // float targetCenterY = target.getCenterY(); // Already defined above
                                ScoreEffect effect = new ScoreEffect(scoreTexture, targetCenterX, targetCenterY, 0.75f); // 0.75 second fade
                                activeScoreEffects.add(effect);
                            }
                            
                            // submitScore(); // Moved to end of level
                            break; // Assume one bullet hits one target
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePause();
                    return true;
                }
                return false;
            }
        };
    }

    private void togglePause() {
        isPaused = !isPaused;
        Gdx.input.setCursorCatched(!isPaused); // Show cursor when paused
        if (isPaused) {
            levelManager.pauseMusic(); // Assuming LevelManager has pause/resume music
        } else {
            levelManager.resumeMusic();
        }
    }

    private void resumeGame() {
        isPaused = false;
        Gdx.input.setCursorCatched(true);
        levelManager.resumeMusic();
    }

    private void quitToLevelSelect() {
        if (hasReturnedToLevelSelect) {
            Gdx.app.log("GameScreen", "Already transitioning/returned to Level Select. Ignoring quit action.");
            return;
        }
        hasReturnedToLevelSelect = true;

        Gdx.app.log("GameScreen", "Quitting to Level Select Screen from pause menu.");
        // Ensure cursor is visible and input processor is cleared before changing screens
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(null); // Important to clear processor for the current screen
        levelManager.stopLevel(); // Clean up level manager state (stops music, disposes current music)
        game.setScreen(new LevelSelectScreen(game, player));
        dispose(); // Dispose current game screen resources
    }

    private void returnToLevelSelectScreen() {
        if (hasReturnedToLevelSelect) {
            // This check might be redundant if called carefully, but good for safety.
            // Gdx.app.postRunnable might queue this multiple times if not careful before this flag is set.
            // However, the primary guard is in the calling locations (submitScore callbacks).
            Gdx.app.log("GameScreen", "Already transitioning/returned to Level Select. Ignoring duplicate call to returnToLevelSelectScreen.");
            return;
        }
        // This method is intended to be called via Gdx.app.postRunnable,
        // so this flag is set on the main thread before any potential duplicate runnable executes.
        hasReturnedToLevelSelect = true; 

        Gdx.app.log("GameScreen", "Returning to Level Select Screen automatically after level completion.");
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(null);
        levelManager.stopLevel(); // Clean up level manager state (stops music, disposes current music)
        game.setScreen(new LevelSelectScreen(game, player));
        dispose(); // Dispose this GameScreen's resources
    }

    private void submitScore() {
        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url("http://localhost:3000/api/scores/submit")
            .header("Content-Type", "application/json")
            .content("{\"player\":{\"id\":\"" + player.getId() + "\"}," +
                     "\"level\":{\"id\":\"" + this.currentLevelId + "\"}," + // Use the actual level ID
                     "\"score\":" + currentScore + "," +
                     "\"playerLevel\":" + 0 + "}") // Sending playerLevel as 0, adjust if a different value is needed
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("Score", "Score submitted: " + currentScore);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (!hasReturnedToLevelSelect) { // Check flag before transitioning
                           returnToLevelSelectScreen();
                        }
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("Score", "Failed to submit score", t);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                         if (!hasReturnedToLevelSelect) { // Check flag before transitioning
                           returnToLevelSelectScreen();
                        }
                    }
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.log("Score", "Score submission cancelled.");
                 Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                         if (!hasReturnedToLevelSelect) { // Check flag before transitioning
                           returnToLevelSelectScreen();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        if (!isPaused) {
            gameAssets.update(delta);
            updateGameState(delta);
        }
        
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        drawGame(); // Draw game elements first

        if (isPaused) {
            drawPauseMenu(); // Then draw pause menu on top
        }
    }

    private void updateGameState(float delta) {
        if (isPaused) return; // Don't update game state if paused

        crosshair.updatePosition();

        float targetX = crosshair.getCenterX() + gunOffsetX;
        gunPosition.x = MathUtils.lerp(gunPosition.x, targetX, delta * (1.0f / gunLagFactor));
        gunPosition.y = fixedGunY;

        gun.setPosition(gunPosition.x, gunPosition.y);
        gun.update(delta);

        // Update bullet casings
        for (int i = bulletCasings.size - 1; i >= 0; i--) {
            Bullet casing = bulletCasings.get(i);
            casing.update(delta);
            if (casing.isAnimationFinished()) {
                bulletCasings.removeIndex(i);
            }
        }

        // Update targets
        List<Target> newTargets = levelManager.update(delta);
        activeTargets.addAll(newTargets);
        activeTargets.removeIf(target -> target.update(delta)); // Targets remove themselves if destroyed or timed out

        // Update score effects
        Iterator<ScoreEffect> effectIterator = activeScoreEffects.iterator();
        while (effectIterator.hasNext()) {
            ScoreEffect effect = effectIterator.next();
            if (!effect.update(delta)) { // update returns false if effect is finished
                effectIterator.remove();
            }
        }

        // Check for level completion and submit score
        if (!scoreSubmittedThisAttempt && levelManager.isSpawningComplete()) {
            // Spawning is done. Now check if music has finished OR if all active targets are also cleared.
            // Using music finishing as the primary trigger after spawning is complete.
            if (!levelManager.isMusicPlaying()) {
                 // As an additional condition, you might want to wait for activeTargets.isEmpty()
                 // if targets can exist after music stops. For now, music stop + spawn complete is the trigger.
                if (activeTargets.isEmpty()) { // Let's wait for targets to clear too
                    Gdx.app.log("GameScreen", "Level complete! Spawning finished, music stopped, and targets cleared. Final score: " + currentScore);
                    // Set flag before calling submitScore, which now handles the transition.
                    scoreSubmittedThisAttempt = true; 
                    submitScore(); 
                    // The transition to LevelSelectScreen will be handled by submitScore's callbacks.
                } else if (!levelManager.isMusicPlaying() && levelManager.isSpawningComplete() && !activeTargets.isEmpty()){
                    // This case: Spawning done, music stopped, but targets still on screen.
                    // If we want to end the level here regardless of remaining targets (e.g. music ended is the hard stop)
                    // then we could also call submitScore() here.
                    // For now, the logic requires activeTargets to be empty.
                    Gdx.app.log("GameScreen", "Music ended and spawning complete, but targets still active. Waiting for targets to clear.");
                    // If targets have very long lifetimes, this might feel like the game is stuck.
                    // Consider adding a timeout for remaining targets after music ends if this becomes an issue.
                }
            }
        }
    }

    // This is the single, correct drawGame method.
    // The previous diff operation incorrectly duplicated parts of updateGameState and drawGame.
    private void drawGame() {
        batch.begin();

        // Draw backgrounds
        if (bar2Texture != null) {
            batch.draw(bar2Texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (bar1Texture != null) {
            float screenWidth = Gdx.graphics.getWidth();
            float originalBar1Width = bar1Texture.getWidth();
            float originalBar1Height = bar1Texture.getHeight();
            float scaledBar1Height = originalBar1Height * (screenWidth / originalBar1Width);
            float bar1YOffset = -30f;

            // Draw targets
            for (Target target : activeTargets) {
                target.render(batch);
            }

            // Draw score effects
            for (ScoreEffect effect : activeScoreEffects) {
                effect.render(batch);
            }

            batch.draw(bar1Texture, 0, bar1YOffset, screenWidth, scaledBar1Height);
        }

        // Draw gun
        TextureRegion currentGunFrame = gun.isAnimating() ? gameAssets.getCurrentGunFrame() : staticGunTextureRegion;
        float gunFrameWidth = currentGunFrame.getRegionWidth();
        float gunFrameHeight = currentGunFrame.getRegionHeight();
        float originX = gunFrameWidth / 2f;
        float originY = gunFrameHeight;

        // Calculate gun tilt
        float cursorY = crosshair.getCenterY();
        float screenHeight = Gdx.graphics.getHeight();
        float gunToScreenTop = screenHeight - fixedGunY;
        float relativeCursorY = cursorY - fixedGunY;
        float tiltRatio = gunToScreenTop > 0 ? Math.max(0f, Math.min(1f, relativeCursorY / gunToScreenTop)) : 0f;
        float currentTilt = maxTiltAngle * tiltRatio;

        batch.draw(currentGunFrame,
                   gunPosition.x - originX,
                   gunPosition.y,
                   originX, originY,
                   gunFrameWidth, gunFrameHeight,
                   gunScale, gunScale,
                   currentTilt);

        // Draw bullet casings
        for (Bullet casing : bulletCasings) {
            casing.render(batch);
        }

        crosshair.render(batch);

        // Draw Score Tracker
        if (scoreFont != null) {
            String scoreText = "Score: " + currentScore;
            glyphLayout.setText(scoreFont, scoreText);
            float scoreX = Gdx.graphics.getWidth() - glyphLayout.width - 20; // 20px padding from right
            float scoreY = glyphLayout.height + 20; // 20px padding from bottom
            scoreFont.draw(batch, scoreText, scoreX, scoreY);
        }

        batch.end();
    }

    private void drawPauseMenu() {
        batch.begin();
        // Draw semi-transparent overlay
        batch.setColor(0, 0, 0, 0.7f); // Black with 70% opacity
        if (pauseOverlayTexture != null) {
             batch.draw(pauseOverlayTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.setColor(Color.WHITE); // Reset color

        // Draw "Paused" text
        if (scoreFont != null) { // Reusing scoreFont for simplicity
            String pausedText = "Paused";
            glyphLayout.setText(scoreFont, pausedText);
            scoreFont.draw(batch, pausedText, Gdx.graphics.getWidth() / 2f - glyphLayout.width / 2f, Gdx.graphics.getHeight() * 0.75f);

            // Draw "Resume" button text
            String resumeText = "Resume (Click or ESC)";
            glyphLayout.setText(scoreFont, resumeText);
            scoreFont.draw(batch, resumeText, Gdx.graphics.getWidth() / 2f - glyphLayout.width / 2f, Gdx.graphics.getHeight() / 2f + 25);
            
            // Draw "Quit to Level Select" button text
            String quitText = "Quit to Level Select";
            glyphLayout.setText(scoreFont, quitText);
            scoreFont.draw(batch, quitText, Gdx.graphics.getWidth() / 2f - glyphLayout.width / 2f, Gdx.graphics.getHeight() / 2f - 25);
        }
        batch.end();
    }


    @Override
    public void resize(int width, int height) {
        // Method is currently empty, but correctly defined.
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true); // Ensure cursor is catched when screen is shown
        Gdx.input.setInputProcessor(gameInputAdapter); // Set the input processor for this screen
    }

    @Override
    public void hide() {
        Gdx.input.setCursorCatched(false); // Release cursor when screen is hidden
        Gdx.input.setInputProcessor(null); // Clear the input processor
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        // Gdx.input.setCursorCatched(false); // Moved to hide()
        batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (gunshotSound != null) gunshotSound.dispose();
        if (gunpingSound != null) gunpingSound.dispose();
        // if (scoreFont != null) scoreFont.dispose(); // If GameAssets manages font, it should dispose it.
                                                    // If created here, dispose here.
        if (pauseOverlayTexture != null && pauseOverlayTexture.getTexture() != null) {
            // Only dispose if this screen created the texture.
            // If it's from GameAssets, GameAssets should manage its lifecycle.
            // For the placeholder libgdx.png, it's fine, but for a programmatically created one:
            // pauseOverlayTexture.getTexture().dispose();
        }
    }
}
