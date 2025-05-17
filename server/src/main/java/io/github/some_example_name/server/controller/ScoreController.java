package io.github.some_example_name.server.controller;

import io.github.some_example_name.server.model.Score;
import io.github.some_example_name.server.service.PlayerService;
import io.github.some_example_name.server.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for score-related endpoints.
 */
@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScoreController {
    @Autowired
    private ScoreService scoreService;
    
    @Autowired
    private PlayerService playerService;

    /**
     * Get all scores.
     */
    @GetMapping
    public ResponseEntity<List<Score>> getAllScores() {
        List<Score> scores = scoreService.getAllScores();
        return ResponseEntity.ok(scores);
    }

    /**
     * Get score by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getScoreById(@PathVariable Long id) {
        return scoreService.getScoreById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get scores by player.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Score>> getScoresByPlayer(@PathVariable Long playerId) {
        List<Score> scores = scoreService.getScoresByPlayer(playerId);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get scores by map.
     */
    @GetMapping("/map/{mapId}")
    public ResponseEntity<List<Score>> getScoresByMap(@PathVariable Long mapId) {
        List<Score> scores = scoreService.getScoresByMap(mapId);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get top scores for a map.
     */
    @GetMapping("/map/{mapId}/top")
    public ResponseEntity<Page<Score>> getTopScoresForMap(
            @PathVariable Long mapId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Score> scores = scoreService.getTopScoresForMap(mapId, pageable);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get personal bests for current player.
     */
    @GetMapping("/me/bests")
    public ResponseEntity<?> getMyPersonalBests(@AuthenticationPrincipal UserDetails userDetails) {
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .map(player -> {
                List<Score> scores = scoreService.getPersonalBests(player.getId());
                return ResponseEntity.ok(scores);
            })
            .orElse(ResponseEntity.status(404).body("Player not found"));
    }

    /**
     * Get personal bests for a player.
     */
    @GetMapping("/player/{playerId}/bests")
    public ResponseEntity<List<Score>> getPersonalBests(@PathVariable Long playerId) {
        List<Score> scores = scoreService.getPersonalBests(playerId);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get recent scores for current player.
     */
    @GetMapping("/me/recent")
    public ResponseEntity<?> getMyRecentScores(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .map(player -> {
                Pageable pageable = PageRequest.of(page, size);
                Page<Score> scores = scoreService.getRecentScores(player.getId(), pageable);
                return ResponseEntity.ok(scores);
            })
            .orElse(ResponseEntity.status(404).body("Player not found"));
    }

    /**
     * Get recent scores for a player.
     */
    @GetMapping("/player/{playerId}/recent")
    public ResponseEntity<Page<Score>> getRecentScores(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Score> scores = scoreService.getRecentScores(playerId, pageable);
        return ResponseEntity.ok(scores);
    }

    /**
     * Get global leaderboard.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Page<Score>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Score> scores = scoreService.getGlobalLeaderboard(pageable);
        return ResponseEntity.ok(scores);
    }

    /**
     * Submit a new score for current player.
     */
    @PostMapping("/submit/{mapId}")
    public ResponseEntity<?> submitScore(
            @PathVariable Long mapId,
            @RequestBody Score score,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .map(player -> {
                Score submittedScore = scoreService.submitScore(score, player.getId(), mapId);
                return ResponseEntity.ok(submittedScore);
            })
            .orElse(ResponseEntity.status(404).body("Player not found"));
    }

    /**
     * Delete a score (admin only or score owner).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScore(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return scoreService.getScoreById(id)
            .map(score -> {
                // Check if current user is the score owner
                if (score.getPlayer().getUsername().equals(userDetails.getUsername())) {
                    scoreService.deleteScore(id);
                    return ResponseEntity.ok("Score deleted successfully");
                } else {
                    return ResponseEntity.status(403).body("You don't have permission to delete this score");
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
