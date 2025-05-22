package io.github.some_example_name.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.assets.GameAssets;

/**
 * Represents a target in the game
 */
public class Target {
    private static final float TARGET_SCALE = 0.3f; // Scale factor for the target
    private static final float GRAVITY = -350.0f; // Gravity effect in pixels/second^2 (negative for downward pull)
    
    private float x, y;
    private float velocityY;
    private Texture texture; 
    private Rectangle bounds;
    private int scoreValue;
    private GameAssets assets;
    private boolean isDestroyed = false;

    private float destructionAnimationTime = 0f; // Each target manages its own animation time
    private Animation<TextureRegion> destructionAnimation; // Reference to the destruction animation

    /**
     * Creates a new target
     * @param x X position
     * @param y Y position
     * @param type Target type (1-4) - determines score value
     * @param velocityY Initial vertical speed of the target (positive for upwards)
     */
    public Target(float x, float y, String type, float velocityY) {
        this.x = x;
        this.y = y;
        this.velocityY = velocityY;
        this.assets = GameAssets.getInstance();
        this.destructionAnimation = assets.getTargetAnimation(); // Get the animation object
        
        this.texture = assets.getTargetTexture("target1"); // Default texture before destruction
        
        float scaledWidth = this.texture.getWidth() * TARGET_SCALE;
        float scaledHeight = this.texture.getHeight() * TARGET_SCALE;
        this.bounds = new Rectangle(x, y, scaledWidth, scaledHeight);
        
        switch(type) {
            case "1": scoreValue = 100; break;
            case "2": scoreValue = 300; break;
            case "3": scoreValue = 600; break;
            case "4": scoreValue = 1000; break;
            default: scoreValue = 100;
        }
    }

    /**
     * Updates the target state
     * @param deltaTime Time since last frame in seconds
     * @return True if the target should be removed from the game
     */
    public boolean update(float deltaTime) {
        velocityY += GRAVITY * deltaTime;
        y += velocityY * deltaTime;
        bounds.setPosition(x, y);

        if (isDestroyed) {
            destructionAnimationTime += deltaTime; // Increment this target's animation time
            // Remove if animation is finished AND fallen off screen
            if (destructionAnimation.isAnimationFinished(destructionAnimationTime)) {
                if (y + bounds.height < 0) { 
                    return true; 
                }
            }
            return false; // Keep updating if animation not finished or still on screen
        } else {
            // If not destroyed, remove if off-screen (top or bottom)
            if (y > Gdx.graphics.getHeight() || y + bounds.height < 0) {
                return true;
            }
            return false; 
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
            currentFrameToDraw = assets.getCurrentTargetFrame(destructionAnimationTime); 
            if (currentFrameToDraw == null) return; 
            frameWidth = currentFrameToDraw.getRegionWidth();
            frameHeight = currentFrameToDraw.getRegionHeight();
        } else {
            currentFrameToDraw = new TextureRegion(this.texture); 
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
        if (isDestroyed) return false;
        return bounds.contains(pointX, pointY);
    }
    
    /**
     * Marks this target as destroyed and starts its destruction animation timer.
     */
    public void destroy() {
        if (!isDestroyed) {
            isDestroyed = true;
            destructionAnimationTime = 0f; // Reset timer for this specific target's animation
        }
    }
    
    public boolean isDestroyed() {
        return isDestroyed;
    }
    
    public int getScoreValue() {
        return scoreValue;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
