package io.github.some_example_name.server.service;

import io.github.some_example_name.server.model.GameMap;
import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.model.Score;
import io.github.some_example_name.server.repository.GameMapRepository;
import io.github.some_example_name.server.repository.PlayerRepository;
import io.github.some_example_name.server.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Score entities.
 */
@Service
public class ScoreService {
    @Autowired
    private ScoreRepository scoreRepository;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private GameMapRepository gameMapRepository;
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * Get all scores.
     */
    public List<Score> getAllScores() {
        return scoreRepository.findAll();
    }
    
    /**
     * Get score by ID.
     */
    public Optional<Score> getScoreById(Long id) {
        return scoreRepository.findById(id);
    }
    
    /**
     * Get scores by player.
     */
    public List<Score> getScoresByPlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        return scoreRepository.findByPlayer(player);
    }
    
    /**
     * Get scores by map.
     */
    public List<Score> getScoresByMap(Long mapId) {
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        return scoreRepository.findByMap(map);
    }
    
    /**
     * Get top scores for a map.
     */
    public Page<Score> getTopScoresForMap(Long mapId, Pageable pageable) {
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        return scoreRepository.findByMapOrderByScoreDesc(map, pageable);
    }
    
    /**
     * Get personal bests for a player.
     */
    public List<Score> getPersonalBests(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        return scoreRepository.findPersonalBests(player);
    }
    
    /**
     * Get recent scores for a player.
     */
    public Page<Score> getRecentScores(Long playerId, Pageable pageable) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        return scoreRepository.findByPlayerOrderByTimestampDesc(player, pageable);
    }
    
    /**
     * Get global leaderboard.
     */
    public Page<Score> getGlobalLeaderboard(Pageable pageable) {
        return scoreRepository.findGlobalLeaderboard(pageable);
    }
    
    /**
     * Submit a new score.
     */
    @Transactional
    public Score submitScore(Score score, Long playerId, Long mapId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        
        score.setPlayer(player);
        score.setMap(map);
        
        // Calculate accuracy if not set
        if (score.getAccuracy() == 0) {
            int totalShots = score.getHits() + score.getMisses();
            if (totalShots > 0) {
                score.setAccuracy((float) score.getHits() / totalShots * 100);
            }
        }
        
        // Update player stats
        int totalShots = score.getHits() + score.getMisses();
        int playtimeInSeconds = map.getDuration(); // Using map duration as approximate playtime
        playerService.updatePlayerStats(playerId, totalShots, score.getHits(), playtimeInSeconds);
        
        return scoreRepository.save(score);
    }
    
    /**
     * Delete a score by ID.
     */
    public void deleteScore(Long id) {
        scoreRepository.deleteById(id);
    }
}
