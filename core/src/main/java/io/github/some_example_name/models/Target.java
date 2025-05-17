package io.github.some_example_name.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.assets.GameAssets;

/**
 * Represents a target in the game
 */
public class Target {    
    
    private float x, y;
    private Texture texture;
    private Rectangle bounds;
    private int scoreValue;
    private GameAssets assets;
    // Destruction animation state
    private boolean isDestroyed = false;
      /**
     * Creates a new target
     * @param x X position
     * @param y Y position
     * @param type Target type (1-4) - determines score value
     */
    public Target(float x, float y, String type) {
        this.x = x;
        this.y = y;
        this.assets = GameAssets.getInstance();
        
        // Set target texture based on type (always use target1 initially)
        this.texture = assets.getTargetTexture("target1");
        
        // Create bounds for hit detection
        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
        
        // Set score value based on target type
        switch(type) {
            case "1":
                scoreValue = 100;
                break;
            case "2":
                scoreValue = 300;
                break;
            case "3":
                scoreValue = 600;
                break;
            case "4":
                scoreValue = 1000;
                break;
            default:
                scoreValue = 100;
        }
    }
      /**
     * Updates the target state
     * @param deltaTime Time since last frame
     * @return True if the target should be removed from the game
     */
    public boolean update(float deltaTime) {
        if (isDestroyed && assets.isTargetAnimationFinished()) {
            return true; // Target animation finished, can be removed
        }
        return false;
    }
    
    /**
     * Draws the target
     * @param batch SpriteBatch to draw with
     */
    public void render(SpriteBatch batch) {
        if (isDestroyed) {
            // Draw the current animation frame
            TextureRegion currentFrame = assets.getCurrentTargetFrame();
            batch.draw(currentFrame, x, y);
        } else {
            // Draw the normal target
            batch.draw(texture, x, y);
        }
    }
    
    /**
     * Checks if a point hits this target
     * @param pointX X coordinate
     * @param pointY Y coordinate
     * @return True if the point is within the target bounds
     */
    public boolean isHit(float pointX, float pointY) {
        // Don't allow hits on already destroyed targets
        if (isDestroyed) return false;
        
        return bounds.contains(pointX, pointY);
    }
    
    /**
     * Marks this target as destroyed and starts the destruction animation
     */
    public void destroy() {
        if (!isDestroyed) {
            isDestroyed = true;
            assets.startTargetAnimation();
        }
    }
    
    /**
     * Checks if this target is destroyed
     * @return True if the target is destroyed
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }
    
    /**
     * Gets the score value of this target
     * @return Score value
     */
    public int getScoreValue() {
        return scoreValue;
    }
    
    /**
     * Gets the X position
     * @return X position
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the Y position
     * @return Y position
     */
    public float getY() {
        return y;
    }
}
