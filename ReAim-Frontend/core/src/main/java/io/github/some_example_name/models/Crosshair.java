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
    private float scale; // Scale factor for the crosshair

    /**
     * Creates a new crosshair with a default scale.
     * @param type Crosshair type (1-3)
     */
    public Crosshair(String type) {
        this(type, 0.5f); // Default scale to 0.5f (half size)
    }

    /**
     * Creates a new crosshair with a specific scale.
     * @param type Crosshair type (1-3)
     * @param scale The scale factor for the crosshair size (e.g., 0.5 for half size)
     */
    public Crosshair(String type, float scale) {
        this.type = type;
        this.scale = scale;
        this.isReady = false;
        this.assets = GameAssets.getInstance();
        
        // Load textures
        this.normalTexture = assets.getCrosshairTexture("crosshair" + type);
        if (this.normalTexture == null) {
            Gdx.app.error("Crosshair", "Failed to load normal texture for type: " + type + ". Using fallback.");
            // Attempt to load a very generic placeholder if specific one fails
            this.normalTexture = new Texture(Gdx.files.internal("badlogic.jpg")); 
        }
        
        this.readyTexture = assets.getCrosshairTexture("crosshair" + type + "R");
        if (this.readyTexture == null) {
            Gdx.app.error("Crosshair", "Failed to load ready texture for type: " + type + "R. Using normal texture as fallback.");
            this.readyTexture = this.normalTexture; // Fallback to normal if red version is missing
        }
        
        // Set initial position
        updatePosition();
    }
    
    /**
     * Updates the crosshair position to match cursor, accounting for scale.
     */
    public void updatePosition() {
        if (normalTexture == null) {
            Gdx.app.error("Crosshair", "Cannot updatePosition, normalTexture is null.");
            return; 
        }
        float scaledWidth = normalTexture.getWidth() * scale;
        float scaledHeight = normalTexture.getHeight() * scale;
        
        x = Gdx.input.getX() - scaledWidth / 2;
        y = Gdx.graphics.getHeight() - Gdx.input.getY() - scaledHeight / 2;
    }
    
    /**
     * Draws the crosshair, scaled.
     * @param batch SpriteBatch to draw with
     */
    public void render(SpriteBatch batch) {
        if (normalTexture == null) {
            Gdx.app.error("Crosshair", "Cannot render, normalTexture is null.");
            return;
        }
        Texture currentTexture = isReady ? readyTexture : normalTexture;
        // Ensure currentTexture is not null if readyTexture was also null and normalTexture was the fallback
        if (currentTexture == null) { 
            currentTexture = normalTexture; 
        }

        if (currentTexture != null) { // Final check
            float scaledWidth = currentTexture.getWidth() * scale;
            float scaledHeight = currentTexture.getHeight() * scale;
            batch.draw(currentTexture, x, y, scaledWidth, scaledHeight);
        } else {
            Gdx.app.error("Crosshair", "Cannot render, currentTexture is null even after fallbacks.");
        }
    }
    
    /**
     * Sets whether the crosshair is over a valid target
     * @param isReady True if ready to shoot (over target)
     */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
    
    /**
     * Gets the X position for hit detection, accounting for scale.
     * @return Center X position
     */
    public float getCenterX() {
        if (normalTexture == null) {
            Gdx.app.log("Crosshair", "getCenterX called with null normalTexture. Returning raw x."); // Changed warn to log
            return x; 
        }
        return x + (normalTexture.getWidth() * scale) / 2;
    }
    
    /**
     * Gets the Y position for hit detection, accounting for scale.
     * @return Center Y position
     */
    public float getCenterY() {
        if (normalTexture == null) {
            Gdx.app.log("Crosshair", "getCenterY called with null normalTexture. Returning raw y."); // Changed warn to log
            return y;
        }
        return y + (normalTexture.getHeight() * scale) / 2;
    }
    
    /**
     * Change the crosshair type
     * @param type New crosshair type (1-3)
     */
    public void setType(String type) {
        this.type = type;
        // Reload textures with the new type, maintaining current scale
        this.normalTexture = assets.getCrosshairTexture("crosshair" + type);
        if (this.normalTexture == null) {
            Gdx.app.error("Crosshair", "Failed to load normal texture for type: " + type + " during setType. Using fallback.");
            this.normalTexture = new Texture(Gdx.files.internal("badlogic.jpg"));
        }
        this.readyTexture = assets.getCrosshairTexture("crosshair" + type + "R");
        if (this.readyTexture == null) {
            Gdx.app.error("Crosshair", "Failed to load ready texture for type: " + type + "R during setType. Using normal as fallback.");
            this.readyTexture = this.normalTexture;
        }
        updatePosition(); // Recalculate position based on new texture sizes (if they changed)
    }
}
