package io.github.some_example_name.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.assets.GameAssets;

/**
 * Represents a target in the game
 */
public class Target {
    private static final float TARGET_SCALE = 0.3f; // Scale factor for the target
    
    private float x, y;
    private float velocityY;
    private Texture texture; // Keep for non-animated state if needed, or remove if always animated
    private Rectangle bounds;
    private int scoreValue;
    private GameAssets assets;
    private boolean isDestroyed = false;

    /**
     * Creates a new target
     * @param x X position
     * @param y Y position
     * @param type Target type (1-4) - determines score value
     * @param velocityY Vertical speed of the target
     */
    public Target(float x, float y, String type, float velocityY) {
        this.x = x;
        this.y = y;
        this.velocityY = velocityY;
        this.assets = GameAssets.getInstance();
        
        // Set target texture based on type (always use target1 initially)
        // TODO: Make target texture dynamic based on 'type' if different target visuals are needed
        this.texture = assets.getTargetTexture("target1"); // Default texture
        
        // Create bounds for hit detection, using scaled dimensions
        float scaledWidth = this.texture.getWidth() * TARGET_SCALE;
        float scaledHeight = this.texture.getHeight() * TARGET_SCALE;
        this.bounds = new Rectangle(x, y, scaledWidth, scaledHeight);
        
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
        if (isDestroyed) {
            // If destroyed, only update animation state
            return assets.isTargetAnimationFinished(); // Target animation finished, can be removed
        } else {
            // If not destroyed, move the target upwards
            y += velocityY * deltaTime;
            bounds.setPosition(x, y); // Bounds are already scaled, just update position

            // Gdx.app.debug("Target", "Target y: " + y + ", velocityY: " + velocityY + ", deltaTime: " + deltaTime);


            // Check if target is off-screen (top)
            if (y > Gdx.graphics.getHeight()) {
                return true; // Mark for removal
            }
            return false; // Not ready for removal yet
        }
    }
    
    /**
     * Draws the target
     * @param batch SpriteBatch to draw with
     */
    public void render(SpriteBatch batch) {
        TextureRegion currentFrameToDraw;
        float frameWidth, frameHeight;

        if (isDestroyed) {
            currentFrameToDraw = assets.getCurrentTargetFrame(); // Assumes this returns a valid frame
            if (currentFrameToDraw == null) return; // Safety check if animation is not ready
            frameWidth = currentFrameToDraw.getRegionWidth();
            frameHeight = currentFrameToDraw.getRegionHeight();
        } else {
            // For non-destroyed targets, use the static texture
            // If you want non-destroyed targets to also be animated, adjust GameAssets accordingly
            // For now, using the static texture for simplicity before destruction.
            currentFrameToDraw = new TextureRegion(this.texture); // Wrap static texture in TextureRegion
            frameWidth = this.texture.getWidth();
            frameHeight = this.texture.getHeight();
        }
        
        batch.draw(currentFrameToDraw, 
                     x, y, 
                     frameWidth * TARGET_SCALE, 
                     frameHeight * TARGET_SCALE);
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
