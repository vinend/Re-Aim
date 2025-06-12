package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.models.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class LevelManager {
    private static final String LEVELS_CONFIG_PATH = "levels/levels.json";
    private static final String MUSIC_ASSET_PATH = "MUSIC/";
    // private static final float TARGET_TRAVEL_TIME = 2.0f; // Replaced by min/max
    private static final float MIN_TARGET_TRAVEL_TIME = 1.0f; // Further adjusted for higher peaks
    private static final float MAX_TARGET_TRAVEL_TIME = 3.0f; // Further adjusted for more variation and height
    private static final float TARGET_START_Y = 0;
    private static final float TARGET_WIDTH = 64f; // Assuming target width for spawn calculations
    private static final float MIN_HORIZONTAL_SPACING = 120f; // Minimum horizontal distance between targets' left edges

    private Map<String, LevelData> levels;
    private float lastTargetX = -1f; // X-coordinate of the last spawned target
    private Json json;
    private Music currentMusic;
    private LevelData currentLevelData;
    private float levelElapsedTime;
    private int nextTargetIndex;
    // private float targetVelocityY; // Replaced by dynamic calculation

    public LevelManager() {
        levels = new HashMap<>();
        json = new Json();
        Gdx.app.debug("LevelManager", "Attempting to load levels from: " + LEVELS_CONFIG_PATH);
        loadLevels();
        // targetVelocityY = Gdx.graphics.getHeight() / TARGET_TRAVEL_TIME; // Velocity now calculated per target
        Gdx.app.debug("LevelManager", "Loaded " + levels.size() + " levels");
    }

    private void listFilesRecursively(FileHandle dir, String indent) {
        for (FileHandle file : dir.list()) {
            Gdx.app.debug("LevelManager", indent + "- " + file.path());
            if (file.isDirectory()) {
                listFilesRecursively(file, indent + "  ");
            }
        }
    }

    private void loadLevels() {
        FileHandle levelsConfigFile = Gdx.files.internal(LEVELS_CONFIG_PATH);
        String absolutePath = levelsConfigFile.file().getAbsolutePath();
        Gdx.app.debug("LevelManager", "Attempting to load levels from absolute path: " + absolutePath);
        
        if (!levelsConfigFile.exists()) {
            Gdx.app.error("LevelManager", "Levels configuration file not found: " + LEVELS_CONFIG_PATH);
            Gdx.app.error("LevelManager", "Absolute path that failed: " + absolutePath);
            FileHandle assetsDir = Gdx.files.internal("");
            Gdx.app.debug("LevelManager", "Available files in assets directory:");
            listFilesRecursively(assetsDir, "");
            return;
        }

        try {
            JsonValue root = new JsonReader().parse(levelsConfigFile);
            JsonValue levelsArray = root.get("levels");

            if (levelsArray != null && levelsArray.isArray()) {
                for (JsonValue levelJson : levelsArray) {
                    LevelConfig config = json.readValue(LevelConfig.class, levelJson);
                    if (config != null) {
                        Gdx.app.debug("LevelManager", "Loading level: " + config.name);
                        
                        String musicFullPath = MUSIC_ASSET_PATH + config.musicFileName;
                        String analysisFullPath = config.analysisFileName != null ? MUSIC_ASSET_PATH + config.analysisFileName : null;

                        Gdx.app.debug("LevelManager", "Music path: " + musicFullPath);
                        Gdx.app.debug("LevelManager", "Analysis path: " + analysisFullPath);

                        LevelAnalysis levelAnalysis = null;
                        if (analysisFullPath != null) {
                            FileHandle analysisFile = Gdx.files.internal(analysisFullPath);
                            if (analysisFile.exists()) {
                                try {
                                    levelAnalysis = json.fromJson(LevelAnalysis.class, analysisFile);
                                    Gdx.app.log("LevelManager", "Loaded analysis for level: " + config.name);
                                } catch (Exception e) {
                                    Gdx.app.error("LevelManager", "Error parsing analysis file for " + config.name + ":", e); // Print full stack trace
                                }
                            } else {
                                Gdx.app.error("LevelManager", "Analysis file not found for " + config.name + ": " + analysisFullPath);
                            }
                        }

                        LevelData levelData = new LevelData(
                            config.id,
                            config.name,
                            musicFullPath,
                            analysisFullPath,
                            config.difficulty,
                            levelAnalysis
                        );
                        levels.put(config.id, levelData);
                        Gdx.app.log("LevelManager", "Loaded level: " + config.name + " (ID: " + config.id + ")");
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("LevelManager", "Error loading levels from " + LEVELS_CONFIG_PATH + ":", e); // Print full stack trace
        }
    }

    public void startLevel(String levelId) {
        currentLevelData = levels.get(levelId);
        if (currentLevelData == null) {
            Gdx.app.error("LevelManager", "Level not found: " + levelId);
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
        }
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(currentLevelData.getMusicFilePath()));
        currentMusic.play();
        currentMusic.setVolume(0.5f);

        levelElapsedTime = 0;
        nextTargetIndex = 0;
        Gdx.app.log("LevelManager", "Started level: " + currentLevelData.getName());
    }

    public List<Target> update(float deltaTime) {
        List<Target> newTargets = new ArrayList<>();
        if (currentLevelData == null || currentLevelData.getAnalysis() == null || currentMusic == null || !currentMusic.isPlaying()) {
            return newTargets;
        }

        levelElapsedTime += deltaTime;

        Array<TapTarget> tapTargets = currentLevelData.getAnalysis().tap_targets;
        if (tapTargets != null) {
            while (nextTargetIndex < tapTargets.size) {
                TapTarget tapTarget = tapTargets.get(nextTargetIndex);
                // Calculate spawn time based on a random travel time for this target
                float currentTargetTravelTime = MathUtils.random(MIN_TARGET_TRAVEL_TIME, MAX_TARGET_TRAVEL_TIME);
                float spawnTime = tapTarget.time - currentTargetTravelTime;

                if (levelElapsedTime >= spawnTime) {
                    float randomX;
                    int attempts = 0;
                    final int MAX_ATTEMPTS = 10; // Prevent infinite loop if screen is too narrow for spacing

                    do {
                        randomX = MathUtils.random(0, Gdx.graphics.getWidth() - TARGET_WIDTH);
                        attempts++;
                        // Allow spawn if:
                        // 1. It's the first target (lastTargetX == -1f)
                        // 2. The screen is too narrow to enforce spacing meaningfully
                        // 3. The spacing requirement is met
                        if (lastTargetX == -1f ||
                            (Gdx.graphics.getWidth() - TARGET_WIDTH) < MIN_HORIZONTAL_SPACING || // Not enough distinct positions
                            Math.abs(randomX - lastTargetX) >= MIN_HORIZONTAL_SPACING) {
                            break;
                        }
                    } while (attempts < MAX_ATTEMPTS);
                    
                    lastTargetX = randomX; // Update the last spawned target's X position
                    
                    // Calculate initial velocity for this specific target to reach screen top in currentTargetTravelTime
                    // Simplified: v0 = H / t (ignoring gravity for initial upward velocity calculation for simplicity of peak height)
                    // For a more physically accurate peak height with gravity: v0 = (H/t) + (0.5 * g * t)
                    // Where H is screen height, t is currentTargetTravelTime, g is gravity (positive value)
                    // For now, using a simpler approach that ensures varied travel times to peak.
                    // The gravity in Target.java will handle the actual trajectory.
                    // Increased target height by multiplying Gdx.graphics.getHeight()
                    float initialVelocityY = (Gdx.graphics.getHeight() * 1.8f) / currentTargetTravelTime; 
                    
                    Target newTarget = new Target(randomX, TARGET_START_Y, "1", initialVelocityY);
                    newTargets.add(newTarget);
                    Gdx.app.log("LevelManager", "Spawned target at time: " + tapTarget.time + 
                                                " (elapsed: " + levelElapsedTime + 
                                                ", travelTime: " + currentTargetTravelTime +
                                                ", initialVelY: " + initialVelocityY + ")");
                    nextTargetIndex++;
                } else {
                    break;
                }
            }
        }
        return newTargets;
    }

    public void stopLevel() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        currentLevelData = null;
        levelElapsedTime = 0;
        nextTargetIndex = 0;
        Gdx.app.log("LevelManager", "Level stopped.");
    }

    public LevelData getLevel(String levelId) {
        return levels.get(levelId);
    }

    public Map<String, LevelData> getAllLevels() {
        return levels;
    }

    public boolean isSpawningComplete() {
        if (currentLevelData == null || currentLevelData.getAnalysis() == null || 
            currentLevelData.getAnalysis().tap_targets == null || currentLevelData.getAnalysis().tap_targets.isEmpty()) {
            // If there are no targets defined or no level loaded, spawning is effectively complete.
            // Or if music isn't playing (implying level hasn't started properly or ended)
            return true; 
        }
        return nextTargetIndex >= currentLevelData.getAnalysis().tap_targets.size;
    }

    public boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    public static class LevelConfig {
        public String id;
        public String name;
        public String musicFileName;
        public String analysisFileName;
        public String difficulty;
    }

    public static class LevelAnalysis {
        public float tempo;
        public Array<Float> beat_times;
        public Array<Float> onset_times;
        public Array<Float> rms_peak_times;
        public Array<Float> ideal_beat_times; // Added field
        public Array<TapTarget> tap_targets;
        public Array<Object> spike_targets;
        public SubdivisionData subdivisions;
    }

    public static class TapTarget {
        public float time;
        public Array<Float> window;
    }

    public static class SubdivisionData {
        public int count;
        public Array<Float> times;
    }

    public static class LevelData {
        private String id;
        private String name;
        private String musicFilePath;
        private String analysisFilePath;
        private String difficulty;
        private LevelAnalysis analysis;

        public LevelData(String id, String name, String musicFilePath, String analysisFilePath, String difficulty, LevelAnalysis analysis) {
            this.id = id;
            this.name = name;
            this.musicFilePath = musicFilePath;
            this.analysisFilePath = analysisFilePath;
            this.difficulty = difficulty;
            this.analysis = analysis;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getMusicFilePath() { return musicFilePath; }
        public String getAnalysisFilePath() { return analysisFilePath; }
        public String getDifficulty() { return difficulty; }
        public LevelAnalysis getAnalysis() { return analysis; }
    }
}
