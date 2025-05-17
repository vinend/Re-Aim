package io.github.some_example_name.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.assets.GameAssets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Represents a bullet in the game
 */
public class Bullet {
    private float x, y;
    private GameAssets assets;
    private float animationTime = 0f;
    private boolean animationFinished = false;

    /**
     * Creates a new bullet casing.
     * @param x Starting X position (should match the gun's casing ejection point).
     * @param y Starting Y position (should match the gun's casing ejection point).
     */
    public Bullet(float x, float y) {
        this.x = x;
        this.y = y;
        this.assets = GameAssets.getInstance();
        this.assets.resetBulletCasingAnimation(); // Reset animation for each new casing
    }

    /**
     * Updates the bullet casing animation.
     * @param deltaTime Time since last frame.
     */
    public void update(float deltaTime) {
        if (!animationFinished) {
            animationTime += deltaTime;
            if (assets.isBulletCasingAnimationFinished(animationTime)) {
                animationFinished = true;
            }
        }
    }

    /**
     * Draws the bullet casing animation.
     * @param batch SpriteBatch to draw with.
     */
    public void render(SpriteBatch batch) {
        if (!animationFinished) {
            TextureRegion currentFrame = assets.getCurrentBulletCasingFrame(animationTime);
            batch.draw(currentFrame, x, y);
        }
    }

    /**
     * Checks if the bullet casing animation has finished.
     * @return True if the animation is complete.
     */
    public boolean isAnimationFinished() {
        return animationFinished;
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
