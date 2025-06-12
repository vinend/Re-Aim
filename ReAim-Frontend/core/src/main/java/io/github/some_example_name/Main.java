package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.screens.MainMenuScreen;

import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public SpriteBatch batch;
    // This object will hold all the loaded level data
    public Levels levelsData;

    // --- Data Structures for JSON Parsing ---

    /**
     * Represents a single level's data from the JSON file.
     * The field '_id' is used to match the requested key change.
     */
    public static class Level {
        public String _id;
        public String name;
        public String musicFileName;
        public String analysisFileName;
        public String difficulty;
    }

    /**
     * Represents the root JSON object which contains an array of levels.
     */
    public static class Levels {
        public Array<Level> levels;
    }


    @Override
    public void create() {
        batch = new SpriteBatch();

        // 1. Run the Python analysis script to ensure levels.json is up-to-date.
        // The application will wait here until the script is finished.
        runAnalysisScript();
        
        // 3. The game can now proceed to the main menu.
        this.setScreen(new MainMenuScreen(this));
    }

    /**
     * Executes the ANALYZE.py script located in the assets/MUSIC folder.
     * This method blocks until the script has finished its execution.
     * Note: This requires Python to be installed and accessible via the system's PATH.
     */
    private void runAnalysisScript() {
        Gdx.app.log("Main_Analyzer", "Attempting to run Python analysis script...");
        try {
            // Get the directory where the music and script are located.
            // This works when running from the IDE.
            File musicDir = Gdx.files.local("../assets/MUSIC/").file();
            String scriptPath = new File(musicDir, "ANALYZE.py").getAbsolutePath();

            // Check if the script file actually exists before trying to run it
            if (!new File(scriptPath).exists()) {
                Gdx.app.error("Main_Analyzer", "ANALYZE.py not found at: " + scriptPath);
                return;
            }

            // Use ProcessBuilder for better control over the external process.
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            // Set the script's working directory so it can find the MP3s and data folder.
            pb.directory(musicDir); 
            // Merge the error stream with the standard output stream for easier reading.
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read the output from the Python script to display it in the Java console.
            // This is very useful for debugging the Python script's execution.
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Gdx.app.log("PythonAnalyzer", line);
            }

            // Wait for the script to finish. This is CRUCIAL to ensure
            // levels.json is ready before the next step.
            int exitCode = process.waitFor();
            Gdx.app.log("Main_Analyzer", "Analysis script finished with exit code: " + exitCode);
            
            if (exitCode != 0) {
                Gdx.app.error("Main_Analyzer", "The analysis script may have failed. Check the logs above.");
            }

        } catch (Exception e) {
            Gdx.app.error("Main_Analyzer", "Failed to run Python script. Is Python installed and in your system's PATH?", e);
        }
    }


    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        // Good practice to check if a screen exists before disposing it.
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
