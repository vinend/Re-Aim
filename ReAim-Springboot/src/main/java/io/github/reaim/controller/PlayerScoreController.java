package io.github.reaim.controller;

import io.github.reaim.model.Player;
import io.github.reaim.model.Level;
import io.github.reaim.model.PlayerScore;
import io.github.reaim.repository.PlayerRepository;
import io.github.reaim.repository.LevelRepository;
import io.github.reaim.repository.PlayerScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*")
public class PlayerScoreController {

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LevelRepository levelRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitScore(@RequestBody PlayerScore score) {
        Player player = playerRepository.findById(score.getPlayer().getId()).orElse(null);
        Level level = levelRepository.findById(score.getLevel().getId()).orElse(null);

        if (player == null || level == null) {
            return ResponseEntity.badRequest().body("Player or Level not found");
        }

        score.setPlayer(player);
        score.setLevel(level);
        PlayerScore savedScore = playerScoreRepository.save(score);
        return ResponseEntity.ok(savedScore);
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<PlayerScore>> getPlayerScores(@PathVariable String playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }
        List<PlayerScore> scores = playerScoreRepository.findByPlayer(player);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/level/{levelId}")
    public ResponseEntity<List<PlayerScore>> getLevelScores(@PathVariable String levelId) {
        Level level = levelRepository.findById(levelId).orElse(null);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }
        List<PlayerScore> scores = playerScoreRepository.findByLevel(level);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/leaderboard/{levelId}")
    public ResponseEntity<List<PlayerScore>> getLevelLeaderboard(@PathVariable String levelId) {
        Level level = levelRepository.findById(levelId).orElse(null);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }
        List<PlayerScore> scores = playerScoreRepository.findByLevel(level);
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore())); // Sort by score descending
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/player/{playerId}/level/{levelId}")
    public ResponseEntity<List<PlayerScore>> getPlayerLevelScores(
            @PathVariable String playerId,
            @PathVariable String levelId) {
        Player player = playerRepository.findById(playerId).orElse(null);
        Level level = levelRepository.findById(levelId).orElse(null);

        if (player == null || level == null) {
            return ResponseEntity.notFound().build();
        }

        List<PlayerScore> scores = playerScoreRepository.findByPlayerAndLevel(player, level);
        return ResponseEntity.ok(scores);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScore(@PathVariable String id) {
        PlayerScore score = playerScoreRepository.findById(id).orElse(null);
        if (score == null) {
            return ResponseEntity.notFound().build();
        }

        playerScoreRepository.delete(score);
        return ResponseEntity.ok().body("Score deleted successfully");
    }
}
