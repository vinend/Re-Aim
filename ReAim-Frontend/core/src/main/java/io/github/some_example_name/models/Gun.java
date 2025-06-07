package io.github.some_example_name.models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.assets.GameAssets;

/**
 * Represents a gun in the game with animation capabilities
 */
public class Gun {
    private float x, y;
    private boolean isAnimating;
    private GameAssets assets;
    
    public Gun(float x, float y) {
        this.x = x;
        this.y = y;
        this.isAnimating = false;
        this.assets = GameAssets.getInstance();
    }
    
    /**
     * Gets the X position of the gun
     * @return X position
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the Y position of the gun
     * @return Y position
     */
    public float getY() {
        return y;
    }
    
    /**
     * Updates the gun state
     * @param deltaTime Time since last frame
     */
    public void update(float deltaTime) {
        if (isAnimating && assets.isGunAnimationFinished()) {
            isAnimating = false;
        }
    }
    
    /**
     * Draws the gun to the screen
     * @param batch SpriteBatch to draw with
     */
    public void render(SpriteBatch batch) {
        if (isAnimating) {
            TextureRegion currentFrame = assets.getCurrentGunFrame();
            batch.draw(currentFrame, x, y);
        } else {
            // Draw static gun (first frame)
            batch.draw(assets.getGunTexture("gub1"), x, y);
        }
    }
    
    /**
     * Triggers the shooting animation
     */
    public void shoot() {
        isAnimating = true;
        assets.resetGunAnimation();
    }
    
    /**
     * Sets the position of the gun
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Checks if the gun is animating
     * @return true if animating, false otherwise
     */
    public boolean isAnimating() {
        return this.isAnimating;
    }
}
