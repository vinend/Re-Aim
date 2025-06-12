package io.github.some_example_name.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.Net; // Added for HTTP requests
import com.badlogic.gdx.net.HttpRequestBuilder; // Added for HTTP requests
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.models.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class LevelManager {
    private static final String LEVELS_CONFIG_PATH = "MUSIC/levels/levels.json"; // Updated path
    private static final String MUSIC_ASSET_PATH = "MUSIC/";
    // private static final float TARGET_TRAVEL_TIME = 2.0f; // Replaced by min/max
    private static final float MIN_TARGET_TRAVEL_TIME = 0.5f; // Adjusted for more velocity variation (faster min speed)
    private static final float MAX_TARGET_TRAVEL_TIME = 2.5f; // Adjusted for more velocity variation (slower max speed)
    private static final float TARGET_START_Y = 0;
    // private static final float TARGET_WIDTH = 64f; // Replaced by dynamic defaultTargetActualWidth
    private static final float MIN_HORIZONTAL_SPACING = 120f; // Minimum horizontal distance between targets' left edges

    private Map<String, LevelData> levels;
    private float defaultTargetActualWidth; // Stores the actual width of the default target type "1"
    private float lastTargetX = -1f; // X-coordinate of the last spawned target
    private Json json;
    private Music currentMusic;
    private LevelData currentLevelData;
    private float levelElapsedTime;
    private int nextTargetIndex;
    // private float targetVelocityY; // Replaced by dynamic calculation

    private static LevelManager instance; // Singleton instance

    private LevelManager() { // Private constructor for singleton
        levels = new HashMap<>();
        json = new Json();

        // Determine the actual width of the default target type "1"
        // This requires GameAssets to be initialized if LevelManager is a very early singleton.
        // Assuming GameAssets is available when LevelManager.getInstance() is first called.
        com.badlogic.gdx.graphics.Texture defaultTargetTexture = io.github.some_example_name.assets.GameAssets.getInstance().getTargetTexture("1");
        if (defaultTargetTexture != null) {
            this.defaultTargetActualWidth = defaultTargetTexture.getWidth();
            Gdx.app.log("LevelManager", "Default target actual width (type '1'): " + this.defaultTargetActualWidth);
        } else {
            this.defaultTargetActualWidth = 64f; // Fallback if texture "1" is not found
            Gdx.app.error("LevelManager", "Default target texture (type '1') not found. Using fallback width: " + this.defaultTargetActualWidth);
        }
        
        Gdx.app.debug("LevelManager", "Attempting to load levels from: " + LEVELS_CONFIG_PATH);
        loadLevels();
        // targetVelocityY = Gdx.graphics.getHeight() / TARGET_TRAVEL_TIME; // Velocity now calculated per target
        Gdx.app.debug("LevelManager", "Loaded " + levels.size() + " levels locally.");
        syncLevelsWithBackend(); // Attempt to sync levels with backend
    }

    public static synchronized LevelManager getInstance() { // Public static getter for singleton
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
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
                        // Use actual width of the default target for spawn calculation
                        randomX = MathUtils.random(0, Gdx.graphics.getWidth() - this.defaultTargetActualWidth);
                        attempts++;
                        // Allow spawn if:
                        // 1. It's the first target (lastTargetX == -1f)
                        // 2. The screen is too narrow to enforce spacing meaningfully (using actual width)
                        // 3. The spacing requirement is met
                        if (lastTargetX == -1f ||
                            (Gdx.graphics.getWidth() - this.defaultTargetActualWidth) < MIN_HORIZONTAL_SPACING || // Not enough distinct positions
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

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            Gdx.app.log("LevelManager", "Music paused.");
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            // Check if level is active or if it was stopped before trying to play
            if (currentLevelData != null) { // Ensure a level is supposed to be active
                 currentMusic.play();
                 Gdx.app.log("LevelManager", "Music resumed.");
            } else {
                Gdx.app.log("LevelManager", "Music not resumed, no active level.");
            }
        }
    }

    public void stopMusic() { // Renamed from stopLevel to be more specific if only music stop is needed
        if (currentMusic != null) {
            currentMusic.stop();
            // currentMusic.dispose(); // Dispose only when level truly ends or manager is disposed
            // currentMusic = null; // Keep reference if it might be resumed or re-used by same level restart
            Gdx.app.log("LevelManager", "Music stopped.");
        }
        // Not resetting level data here, as this method is just for stopping music.
        // stopLevel() handles full level state reset.
    }

    private void syncLevelsWithBackend() {
        if (levels.isEmpty()) {
            Gdx.app.log("LevelManagerSync", "No levels loaded locally to sync.");
            return;
        }

        Gdx.app.log("LevelManagerSync", "Starting sync of " + levels.size() + " levels with backend...");

        // We need to iterate over the LevelConfig objects that were parsed,
        // not the LevelData map directly if we want the original config structure for the payload.
        // For simplicity, let's re-parse the JSON here or store LevelConfig objects temporarily.
        // A better way would be to store List<LevelConfig> from loadLevels() then iterate it.
        // For now, let's iterate the LevelData map and reconstruct the necessary payload.

        for (LevelData levelDataEntry : levels.values()) {
            // Reconstruct a simplified object for backend submission based on LevelData
            // Backend expects: id, name, jsonFile (analysisFileName), difficulty. Creator is optional.
            String levelId = levelDataEntry.getId();
            String levelName = levelDataEntry.getName();
            // analysisFilePath is like "MUSIC/level_analysis.json". We need just "level_analysis.json" for jsonFile field.
            String analysisFileName = null;
            if (levelDataEntry.getAnalysisFilePath() != null) {
                FileHandle handle = Gdx.files.internal(levelDataEntry.getAnalysisFilePath());
                analysisFileName = handle.name();
            }
            String difficulty = levelDataEntry.getDifficulty();

            if (analysisFileName == null) {
                Gdx.app.error("LevelManagerSync", "Skipping sync for level " + levelName + " due to missing analysis file name.");
                continue;
            }
            
            // Create JSON payload string
            // Using manual string concatenation for simplicity, but a Json object would be more robust.
            // com.badlogic.gdx.utils.Json gdxJson = new com.badlogic.gdx.utils.Json();
            // String payload = gdxJson.toJson(new BackendLevelPayload(levelId, levelName, analysisFileName, difficulty));
            
            // The backend /api/levels/create endpoint expects a 'creator' object with an 'id'.
            // Without a valid creator.id, the request will likely fail with the current LevelController.
            // If a "system" or default creator ID is available, it should be included here.
            // For example: String systemCreatorId = "some-valid-player-id-for-system-levels";
            // String payload = String.format("{\"id\":\"%s\", \"name\":\"%s\", \"jsonFile\":\"%s\", \"difficulty\":\"%s\", \"creator\":{\"id\":\"%s\"}}",
            // levelId.replace("\"", "\\\""),
            // levelName.replace("\"", "\\\""),
            // analysisFileName.replace("\"", "\\\""),
            // difficulty.replace("\"", "\\\""),
            // systemCreatorId.replace("\"", "\\\""));
            //
            // For now, sending without creator, which will likely cause issues with the provided backend controller.
            // The backend should ideally be modified to handle levels without explicit player creators (e.g., system levels).
            String payload = String.format("{\"id\":\"%s\", \"name\":\"%s\", \"jsonFile\":\"%s\", \"difficulty\":\"%s\"}",
                                           levelId.replace("\"", "\\\""), 
                                           levelName.replace("\"", "\\\""),
                                           analysisFileName.replace("\"", "\\\""),
                                           difficulty.replace("\"", "\\\""));
            Gdx.app.log("LevelManagerSync", "WARNING: Sending level sync payload without creator ID. This may fail with the current backend LevelController.");


            HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
            Net.HttpRequest httpRequest = requestBuilder.newRequest()
                    .method(Net.HttpMethods.POST)
                    .url("http://localhost:3000/api/levels/create") // Ensure this matches your backend
                    .header("Content-Type", "application/json")
                    .content(payload)
                    .build();

            Gdx.app.log("LevelManagerSync", "Attempting to sync level: " + levelName + " with payload: " + payload);

            Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    int statusCode = httpResponse.getStatus().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        Gdx.app.log("LevelManagerSync", "Successfully synced level: " + levelName + ". Response: " + httpResponse.getResultAsString());
                    } else {
                        Gdx.app.error("LevelManagerSync", "Failed to sync level: " + levelName + ". Status: " + statusCode + ", Response: " + httpResponse.getResultAsString());
                    }
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("LevelManagerSync", "Error syncing level: " + levelName, t);
                }

                @Override
                public void cancelled() {
                    Gdx.app.log("LevelManagerSync", "Sync cancelled for level: " + levelName);
                }
            });
        }
    }
    
    // Helper class for JSON payload if using gdx.utils.Json for serialization
    // private static class BackendLevelPayload {
    //     public String id;
    //     public String name;
    //     public String jsonFile;
    //     public String difficulty;
    //     // public Object creator = null; // Explicitly null or omit

    //     public BackendLevelPayload(String id, String name, String jsonFile, String difficulty) {
    //         this.id = id;
    //         this.name = name;
    //         this.jsonFile = jsonFile;
    //         this.difficulty = difficulty;
    //     }
    // }


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
        public float confidence; // Added to match analysis JSON files
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
