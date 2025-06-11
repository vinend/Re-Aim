package io.github.reaim.controller;

import io.github.reaim.model.Level;
import io.github.reaim.model.Player;
import io.github.reaim.repository.LevelRepository;
import io.github.reaim.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/levels")
@CrossOrigin(origins = "*")
public class LevelController {

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createLevel(@RequestBody Level level) {
        Player creator = playerRepository.findById(level.getCreator().getId()).orElse(null);
        if (creator == null) {
            return ResponseEntity.badRequest().body("Creator not found");
        }
        level.setCreator(creator);
        Level savedLevel = levelRepository.save(level);
        return ResponseEntity.ok(savedLevel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLevel(@PathVariable String id) {
        Level level = levelRepository.findById(id).orElse(null);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(level);
    }

    @GetMapping
    public ResponseEntity<List<Level>> getAllLevels() {
        List<Level> levels = levelRepository.findAll();
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Level>> getLevelsByCreator(@PathVariable String creatorId) {
        Player creator = playerRepository.findById(creatorId).orElse(null);
        if (creator == null) {
            return ResponseEntity.notFound().build();
        }
        List<Level> levels = levelRepository.findByCreator(creator);
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Level>> getLevelsByDifficulty(@PathVariable String difficulty) {
        List<Level> levels = levelRepository.findByDifficulty(difficulty);
        return ResponseEntity.ok(levels);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLevel(@PathVariable String id, @RequestBody Level levelDetails) {
        Level existingLevel = levelRepository.findById(id).orElse(null);
        if (existingLevel == null) {
            return ResponseEntity.notFound().build();
        }

        existingLevel.setName(levelDetails.getName());
        existingLevel.setJsonFile(levelDetails.getJsonFile());
        existingLevel.setDifficulty(levelDetails.getDifficulty());

        Level updatedLevel = levelRepository.save(existingLevel);
        return ResponseEntity.ok(updatedLevel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLevel(@PathVariable String id) {
        Level level = levelRepository.findById(id).orElse(null);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }

        levelRepository.delete(level);
        return ResponseEntity.ok().body("Level deleted successfully");
    }
}
