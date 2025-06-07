package io.github.some_example_name.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.assets.GameAssets;

/**
 * Represents the player's crosshair/cursor
 */
public class Crosshair {
    private float x, y;
    private String type;
    private Texture normalTexture;
    private Texture readyTexture; // Red version for when over valid target
    private boolean isReady;
    private GameAssets assets;
    
    /**
     * Creates a new crosshair
     * @param type Crosshair type (1-3)
     */
    public Crosshair(String type) {
        this.type = type;
        this.isReady = false;
        this.assets = GameAssets.getInstance();
        
        // Load textures
        this.normalTexture = assets.getCrosshairTexture("crosshair" + type);
        this.readyTexture = assets.getCrosshairTexture("crosshair" + type + "R");
        
        // Set initial position
        updatePosition();
    }
    
    /**
     * Updates the crosshair position to match cursor
     */
    public void updatePosition() {
        // Get mouse position and flip Y coordinate (LibGDX Y is bottom-up)
        x = Gdx.input.getX() - normalTexture.getWidth() / 2;
        y = Gdx.graphics.getHeight() - Gdx.input.getY() - normalTexture.getHeight() / 2;
    }
    
    /**
     * Draws the crosshair
     * @param batch SpriteBatch to draw with
     */
    public void render(SpriteBatch batch) {
        Texture currentTexture = isReady ? readyTexture : normalTexture;
        batch.draw(currentTexture, x, y);
    }
    
    /**
     * Sets whether the crosshair is over a valid target
     * @param isReady True if ready to shoot (over target)
     */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
    
    /**
     * Gets the X position for hit detection
     * @return Center X position
     */
    public float getCenterX() {
        return x + normalTexture.getWidth() / 2;
    }
    
    /**
     * Gets the Y position for hit detection
     * @return Center Y position
     */
    public float getCenterY() {
        return y + normalTexture.getHeight() / 2;
    }
    
    /**
     * Change the crosshair type
     * @param type New crosshair type (1-3)
     */
    public void setType(String type) {
        this.type = type;
        this.normalTexture = assets.getCrosshairTexture("crosshair" + type);
        this.readyTexture = assets.getCrosshairTexture("crosshair" + type + "R");
    }
}
