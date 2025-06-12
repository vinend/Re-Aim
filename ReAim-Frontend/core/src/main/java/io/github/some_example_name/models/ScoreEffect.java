package io.github.some_example_name.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class ScoreEffect {
    private Texture texture;
    private Vector2 position;
    private float alpha;
    private float lifetime; // Total time the effect should last
    private float age;      // Current time elapsed since spawn
    private float width;
    private float height;
    private float scale = 0.5f; // Scale factor for the score effect

    public ScoreEffect(Texture texture, float centerX, float centerY, float lifetime) {
        this.texture = texture;
        if (texture != null) { // Add null check for safety
            this.width = texture.getWidth() * scale;
            this.height = texture.getHeight() * scale;
            this.position = new Vector2(centerX - this.width / 2f, centerY - this.height / 2f);
        } else {
            this.width = 0;
            this.height = 0;
            this.position = new Vector2(centerX, centerY); // Fallback position
        }
        this.lifetime = lifetime;
        this.age = 0f;
        this.alpha = 1.0f;
    }

    public boolean update(float deltaTime) {
        age += deltaTime;
        if (age >= lifetime) {
            alpha = 0f;
            return false; 
        }
        alpha = 1.0f - (age / lifetime);
        alpha = MathUtils.clamp(alpha, 0f, 1f);
        return true; 
    }

    public void render(SpriteBatch batch) {
        if (texture != null && alpha > 0) {
            batch.setColor(1, 1, 1, alpha);
            batch.draw(texture, position.x, position.y, width, height);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public boolean isFinished() {
        return age >= lifetime;
    }
}
