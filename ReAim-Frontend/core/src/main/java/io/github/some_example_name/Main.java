package io.github.some_example_name;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion; // Added for TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils; // Added for lerp and angle calculations
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.assets.GameAssets;
import io.github.some_example_name.models.Bullet;
import io.github.some_example_name.models.Crosshair;
import io.github.some_example_name.models.Gun;
import io.github.some_example_name.models.Target;
import io.github.some_example_name.managers.LevelManager;
import java.util.List;
import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GameAssets gameAssets;

    // Game objects
    private Gun gun;
    private Crosshair crosshair;
    private Array<Bullet> bulletCasings;
    private List<Target> activeTargets;
    private LevelManager levelManager;

    // Gun movement and rendering properties
    private Vector2 gunPosition; // Current visual X, Y position of the gun
    private float gunLagFactor = 0.1f; // Controls the lag for horizontal movement
    private float gunOffsetX = 400f; // Horizontal offset of gun's target X from crosshair's X. Adjust if needed.
    private float fixedGunY = -280f; // Fixed Y position for the gun from the bottom of the screen. Lowered value.
    private float maxTiltAngle = -30f; // Max upward tilt in degrees (negative for up, assuming 0 is horizontal).
    private float gunScale = 0.8f; // Scale factor for the gun sprite. Added.
    private TextureRegion staticGunTextureRegion; // Cache for the static gun frame

    // Background textures
    private Texture bar1Texture;
    private Texture bar2Texture;

    @Override
    public void create() {
        // Enable debug logging
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        gameAssets = GameAssets.getInstance();

        // Hide the mouse cursor
        Gdx.input.setCursorCatched(true);

        // Initialize background textures
        // Ensure these textures are loaded in GameAssets and that GameAssets.dispose() handles them
        bar1Texture = gameAssets.getBarTexture("bar1");
        bar2Texture = gameAssets.getBarTexture("bar2");

        // Initialize crosshair first as its position might be used
        crosshair = new Crosshair("3R");

        // Initialize gun
        gun = new Gun(0, 0); // Initial internal x,y for Gun object, visual is controlled by gunPosition
        gunPosition = new Vector2(Gdx.graphics.getWidth() / 2f, fixedGunY); // Uses updated fixedGunY
        gun.setPosition(gunPosition.x, gunPosition.y); // Set initial position for the Gun object as well

        bulletCasings = new Array<>();
        staticGunTextureRegion = new TextureRegion(gameAssets.getGunTexture("gub1")); // Cache static gun frame
        
        // Initialize level system
        levelManager = new LevelManager();
        activeTargets = new ArrayList<>();
        levelManager.startLevel("aint_that_a_kick_in_the_head");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
                    gun.shoot(); // Gun object handles its animation state and resets GameAsset animation time
                    // Spawn bullet casing at the gun's current visual position
                    bulletCasings.add(new Bullet(gun.getX() - 1250f, gun.getY() + 20f)); // Adjusted offset

                    // Check for target hits
                    float mouseX = crosshair.getCenterX();
                    float mouseY = crosshair.getCenterY();
                    for (Target target : activeTargets) {
                        if (!target.isDestroyed() && target.isHit(mouseX, mouseY)) {
                            target.destroy();
                            break; // Only destroy one target per click
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        gameAssets.update(deltaTime); // Update animation timers in GameAssets

        crosshair.updatePosition(); // Crosshair follows mouse directly

        // Gun's X position lags towards the crosshair's X (with an offset)
        float targetX = crosshair.getCenterX() + gunOffsetX;
        gunPosition.x = MathUtils.lerp(gunPosition.x, targetX, deltaTime * (1.0f / gunLagFactor));
        gunPosition.y = fixedGunY; // Y position is fixed

        gun.setPosition(gunPosition.x, gunPosition.y); // Update the Gun object's internal position
        gun.update(deltaTime); // Update gun's internal animation state

        // Calculate gun tilt
        float cursorY = crosshair.getCenterY();
        float screenHeight = Gdx.graphics.getHeight();
        float gunToScreenTop = screenHeight - fixedGunY;
        float relativeCursorY = cursorY - fixedGunY;
        float tiltRatio = 0f;
        if (gunToScreenTop > 0) { // Avoid division by zero if fixedGunY is at/above screen top
            tiltRatio = Math.max(0f, Math.min(1f, relativeCursorY / gunToScreenTop));
        }
        float currentTilt = maxTiltAngle * tiltRatio;

        // Update and remove finished bullet casings
        for (int i = bulletCasings.size - 1; i >= 0; i--) {
            Bullet casing = bulletCasings.get(i);
            casing.update(deltaTime);
            if (casing.isAnimationFinished()) {
                bulletCasings.removeIndex(i);
            }
        }

        // Update targets
        // Get new targets from level manager
        List<Target> newTargets = levelManager.update(deltaTime);
        activeTargets.addAll(newTargets);

        // Update existing targets and remove those that are finished
        activeTargets.removeIf(target -> target.update(deltaTime));

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();

        // Draw BAR2 first (background layer), covering the whole screen
        if (bar2Texture != null) {
            batch.draw(bar2Texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Draw BAR1 on top of BAR2, scaled to screen width, maintaining aspect ratio, and positioned lower
        if (bar1Texture != null) {
            float screenWidth = Gdx.graphics.getWidth();
            float originalBar1Width = bar1Texture.getWidth();
            float originalBar1Height = bar1Texture.getHeight();

            // Calculate height to maintain aspect ratio when scaled to screen width
            float scaledBar1Height = originalBar1Height * (screenWidth / originalBar1Width);
            
            // Adjust this Y offset to position BAR1 "more below"
            // A negative value moves it downwards from the screen bottom. 0 aligns bottom edge with screen bottom.
            float bar1YOffset = -30f; 

            // Draw targets BEFORE bar1Texture so they appear behind it
            for (Target target : activeTargets) {
                target.render(batch);
            }

            batch.draw(bar1Texture,
                       0,                      // X position (left edge of screen)
                       bar1YOffset,            // Y position (offset from bottom)
                       screenWidth,            // Width (scaled to screen width)
                       scaledBar1Height);      // Height (scaled to maintain aspect ratio)
        }

        // Draw the gun manually with tilt
        TextureRegion currentGunFrame;
        // Assumes Gun.java has public boolean isAnimating()
        if (gun.isAnimating()) { 
            currentGunFrame = gameAssets.getCurrentGunFrame(); 
        } else {
            currentGunFrame = staticGunTextureRegion;
        }

        float gunFrameWidth = currentGunFrame.getRegionWidth();
        float gunFrameHeight = currentGunFrame.getRegionHeight();
        float originX = gunFrameWidth / 2f; // Pivot around horizontal center
        float originY = gunFrameHeight;   // Pivot around bottom edge for tilting up

        batch.draw(currentGunFrame,
                   gunPosition.x - originX, // Adjust draw position because origin is not top-left
                   gunPosition.y,           // Y is already at the desired baseline
                   originX, originY,
                   gunFrameWidth, gunFrameHeight,
                   gunScale, gunScale, // Applied gunScale
                   currentTilt);

        // Render bullet casings
        for (Bullet casing : bulletCasings) {
            casing.render(batch);
        }

        crosshair.render(batch);

        batch.end();

        // Begin ShapeRenderer for drawing lines
        // shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix()); // Ensure ShapeRenderer uses the same camera as SpriteBatch
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // shapeRenderer.setColor(Color.WHITE);

        // Draw lines between the first three active targets if they exist
        // if (activeTargets.size() >= 2) {
        //     Target t0 = activeTargets.get(0);
        //     Target t1 = activeTargets.get(1);
        //     shapeRenderer.line(t0.getCenterX(), t0.getCenterY(), t1.getCenterX(), t1.getCenterY());
            
        //     if (activeTargets.size() >= 3) {
        //         Target t2 = activeTargets.get(2);
        //         shapeRenderer.line(t1.getCenterX(), t1.getCenterY(), t2.getCenterX(), t2.getCenterY());
        //     }
        // }
        // shapeRenderer.end();
    }

    @Override
    public void dispose() {
        // It's good practice to uncatch the cursor when the game closes
        Gdx.input.setCursorCatched(false); 
        batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        gameAssets.dispose(); // GameAssets should handle disposal of all its loaded textures (bar1, bar2, etc.)
        // staticGunTextureRegion does not need disposal if its Texture is managed by gameAssets
        levelManager.stopLevel();
    }
}
