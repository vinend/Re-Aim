package io.github.some_example_name.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;

/**
 * Manages all game assets including textures and animations
 */
public class GameAssets implements Disposable {
    // Singleton instance
    private static GameAssets instance;
    
    // Texture maps for different asset types
    private HashMap<String, Texture> barTextures;
    private HashMap<String, Texture> crosshairTextures;
    private HashMap<String, Texture> bulletTextures;
    private HashMap<String, Texture> gunTextures;
    private HashMap<String, Texture> targetTextures;
    private HashMap<String, Texture> scoreTextures;
    
    // Gun animation
    private Animation<TextureRegion> gunAnimation;
    private float animationTime = 0f;

    // Bullet Casing Animation
    private Animation<TextureRegion> bulletCasingAnimation;
    private float bulletCasingAnimationTime = 0f;
    
    // Target animation (for destruction animation)
    private Animation<TextureRegion> targetAnimation;
    // Removed: private float targetAnimationTime = 0f;
    // Removed: private boolean isTargetAnimating = false;

    // Font
    private BitmapFont defaultFont;
    
    // Private constructor to ensure singleton pattern
    private GameAssets() {
        loadAllAssets();
    }
    
    /**
     * Returns the singleton instance of GameAssets
     */
    public static GameAssets getInstance() {
        if (instance == null) {
            instance = new GameAssets();
        }
        return instance;
    }
    
    /**
     * Loads all game assets
     */
    private void loadAllAssets() {
        // Initialize all hash maps
        barTextures = new HashMap<>();
        crosshairTextures = new HashMap<>();
        bulletTextures = new HashMap<>();
        gunTextures = new HashMap<>();
        targetTextures = new HashMap<>();
        scoreTextures = new HashMap<>();
        
        // Load bar textures
        barTextures.put("bar1", new Texture("BAR/BAR1.png"));
        barTextures.put("bar2", new Texture("BAR/BAR2.png"));
        barTextures.put("barJukebox", new Texture("BAR/BARJUKEBOX.png"));
        barTextures.put("barNote", new Texture("BAR/BARNOTE.png"));
        barTextures.put("barNote2", new Texture("BAR/BARNOTE2.png"));
        barTextures.put("barNote3", new Texture("BAR/BARNOTE3.png"));
        
        // Load crosshair textures
        crosshairTextures.put("crosshair1", new Texture("CROSSHAIRS/CROSSHAIR1.png"));
        crosshairTextures.put("crosshair1R", new Texture("CROSSHAIRS/CROSSHAIR1R.png"));
        crosshairTextures.put("crosshair2", new Texture("CROSSHAIRS/CROSSHAIR2.png"));
        crosshairTextures.put("crosshair2R", new Texture("CROSSHAIRS/CROSSHAIR2R.png"));
        crosshairTextures.put("crosshair3", new Texture("CROSSHAIRS/CROSSHAIR3.png"));
        crosshairTextures.put("crosshair3R", new Texture("CROSSHAIRS/CROSSHAIR3R.png"));
        
        // Load bullet textures
        bulletTextures.put("bullet1", new Texture("GUN & BULLETS/BULLET1.png"));
        bulletTextures.put("bullet2", new Texture("GUN & BULLETS/BULLET2.png"));
        bulletTextures.put("bullet3", new Texture("GUN & BULLETS/BULLET3.png"));
        bulletTextures.put("bullet4", new Texture("GUN & BULLETS/BULLET4.png"));
        
        // Load gun textures for animation
        gunTextures.put("gub1", new Texture("GUN & BULLETS/GUB1.png"));
        gunTextures.put("gub2", new Texture("GUN & BULLETS/GUB2.png"));
        gunTextures.put("gub3", new Texture("GUN & BULLETS/GUB3.png"));
        gunTextures.put("gub4", new Texture("GUN & BULLETS/GUB4.png"));
        
        // Set up gun animation
        TextureRegion[] gunFrames = new TextureRegion[4];
        gunFrames[0] = new TextureRegion(gunTextures.get("gub1"));
        gunFrames[1] = new TextureRegion(gunTextures.get("gub2"));
        gunFrames[2] = new TextureRegion(gunTextures.get("gub3"));
        gunFrames[3] = new TextureRegion(gunTextures.get("gub4"));
        
        // Create gun animation with 0.1 second per frame
        gunAnimation = new Animation<>(0.1f, gunFrames);

        // Set up bullet casing animation
        TextureRegion[] bulletCasingFrames = new TextureRegion[4];
        bulletCasingFrames[0] = new TextureRegion(bulletTextures.get("bullet1"));
        bulletCasingFrames[1] = new TextureRegion(bulletTextures.get("bullet2"));
        bulletCasingFrames[2] = new TextureRegion(bulletTextures.get("bullet3"));
        bulletCasingFrames[3] = new TextureRegion(bulletTextures.get("bullet4"));
        bulletCasingAnimation = new Animation<>(0.1f, bulletCasingFrames); // 0.1s per frame, no loop

        // Load target textures (still keep them individually for reference)
        targetTextures.put("target1", new Texture("TARGET & SCORES/TARGET1.png"));
        targetTextures.put("target2", new Texture("TARGET & SCORES/TARGET2.png"));
        targetTextures.put("target3", new Texture("TARGET & SCORES/TARGET3.png"));
        targetTextures.put("target4", new Texture("TARGET & SCORES/TARGET4.png"));
        
        // Set up target destruction animation
        TextureRegion[] targetFrames = new TextureRegion[4];
        targetFrames[0] = new TextureRegion(targetTextures.get("target1"));
        targetFrames[1] = new TextureRegion(targetTextures.get("target2"));
        targetFrames[2] = new TextureRegion(targetTextures.get("target3"));
        targetFrames[3] = new TextureRegion(targetTextures.get("target4"));
        
        // Create target animation with 0.1 second per frame - this is the destruction animation
        targetAnimation = new Animation<>(0.1f, targetFrames);
        
        // Load score textures
        scoreTextures.put("score100", new Texture("TARGET & SCORES/100.png"));
        scoreTextures.put("score300", new Texture("TARGET & SCORES/300.png"));
        scoreTextures.put("score600", new Texture("TARGET & SCORES/600.png"));
        scoreTextures.put("score1000", new Texture("TARGET & SCORES/1000.png"));

        // Load font
        // The BitmapFont constructor used was likely trying to find "font.png" based on the .fnt file.
        // If "font.fnt" is meant to be used with "uiskin.png", we load it like this:
        defaultFont = new BitmapFont(Gdx.files.internal("ui/font.fnt"), new TextureRegion(new Texture(Gdx.files.internal("ui/uiskin.png"))), false);
        // If "font.fnt" has its own image file that is simply missing, this won't fix the root cause (missing file).
        // However, if "font.fnt" is designed to be used with an atlas image like "uiskin.png", this is a common way.
        // The 'false' argument for flip is typical.
    }
      /**
     * Updates the animation time
     * @param deltaTime Time since last frame
     */
    public void update(float deltaTime) {
        // Update gun animation time
        animationTime += deltaTime;
        bulletCasingAnimationTime += deltaTime;
        
        // Removed target animation time update from here
    }
    
    // Getters for all texture types
    
    public Texture getBarTexture(String name) {
        return barTextures.get(name);
    }
    
    public Texture getCrosshairTexture(String name) {
        return crosshairTextures.get(name);
    }
    
    public Texture getBulletTexture(String name) {
        return bulletTextures.get(name);
    }
    
    public Texture getGunTexture(String name) {
        return gunTextures.get(name);
    }
    
    public Texture getTargetTexture(String name) {
        return targetTextures.get(name);
    }
    
    public Texture getScoreTexture(String name) {
        return scoreTextures.get(name);
    }
    
    /**
     * Gets the current frame of the gun animation
     * @return The current TextureRegion from the gun animation
     */
    public TextureRegion getCurrentGunFrame() {
        return gunAnimation.getKeyFrame(animationTime, true);
    }
    
    /**
     * Checks if the gun animation is finished
     * @return True if the animation has completed one cycle
     */
    public boolean isGunAnimationFinished() {
        return gunAnimation.isAnimationFinished(animationTime);
    }
      /**
     * Resets the gun animation time
     */
    public void resetGunAnimation() {
        animationTime = 0f;
    }
    
    /**
     * Resets the bullet casing animation time.
     */
    public void resetBulletCasingAnimation() {
        bulletCasingAnimationTime = 0f;
    }

    /**
     * Gets the current frame of the bullet casing animation.
     * @param time The current animation time.
     * @return The current TextureRegion from the bullet casing animation.
     */
    public TextureRegion getCurrentBulletCasingFrame(float time) {
        return bulletCasingAnimation.getKeyFrame(time, false); // false = don't loop
    }

    /**
     * Checks if the bullet casing animation is finished.
     * @param time The current animation time.
     * @return True if the animation has completed one cycle.
     */
    public boolean isBulletCasingAnimationFinished(float time) {
        return bulletCasingAnimation.isAnimationFinished(time);
    }

    /**
     * Gets the current frame of the target destruction animation.
     * @param time The current animation time for a specific target.
     * @return The current TextureRegion from the target animation.
     */
    public TextureRegion getCurrentTargetFrame(float time) {
        return targetAnimation.getKeyFrame(time, false); // false = don't loop
    }

    /**
     * Returns the target destruction Animation object.
     * This allows individual targets to check if their animation is finished.
     * @return The Animation<TextureRegion> for target destruction.
     */
    public Animation<TextureRegion> getTargetAnimation() {
        return targetAnimation;
    }

    /**
     * Gets the default font.
     * @param name Identifier for the font (currently unused, returns default).
     * @return The default BitmapFont.
     */
    public BitmapFont getFont(String name) {
        // Currently only one font is loaded, so 'name' parameter is ignored.
        // Could be extended to load multiple fonts if needed.
        return defaultFont;
    }

    /**
     * Disposes of all textures to prevent memory leaks
     */
    @Override
    public void dispose() {
        // Dispose all bar textures
        for (Texture texture : barTextures.values()) {
            texture.dispose();
        }
        
        // Dispose all crosshair textures
        for (Texture texture : crosshairTextures.values()) {
            texture.dispose();
        }
        
        // Dispose all bullet textures
        for (Texture texture : bulletTextures.values()) {
            texture.dispose();
        }
        
        // Dispose all gun textures
        for (Texture texture : gunTextures.values()) {
            texture.dispose();
        }
        
        // Dispose all target textures
        for (Texture texture : targetTextures.values()) {
            texture.dispose();
        }
        
        // Dispose all score textures
        for (Texture texture : scoreTextures.values()) {
            texture.dispose();
        }

        // Dispose font
        if (defaultFont != null) {
            defaultFont.dispose();
        }
    }
}
