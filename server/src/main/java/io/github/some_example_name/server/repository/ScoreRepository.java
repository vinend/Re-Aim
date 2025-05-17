package io.github.some_example_name.server.repository;

import io.github.some_example_name.server.model.GameMap;
import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.model.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Score entity.
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    // Find scores by player
    List<Score> findByPlayer(Player player);
    
    // Find scores by map
    List<Score> findByMap(GameMap map);
    
    // Find scores by player and map
    List<Score> findByPlayerAndMap(Player player, GameMap map);
    
    // Find top scores for a map
    Page<Score> findByMapOrderByScoreDesc(GameMap map, Pageable pageable);
    
    // Find personal bests for a player across all maps
    @Query("SELECT s FROM Score s WHERE s.player = :player AND " +
           "s.score = (SELECT MAX(s2.score) FROM Score s2 WHERE s2.player = :player AND s2.map = s.map)")
    List<Score> findPersonalBests(Player player);
    
    // Find top scores by difficulty
    Page<Score> findByDifficultyOrderByScoreDesc(Score.Difficulty difficulty, Pageable pageable);
    
    // Find recent scores by player
    Page<Score> findByPlayerOrderByTimestampDesc(Player player, Pageable pageable);
    
    // Global leaderboard (top scores across all maps)
    @Query("SELECT s FROM Score s WHERE s.score = " +
           "(SELECT MAX(s2.score) FROM Score s2 WHERE s2.map = s.map AND s2.player = s.player)")
    Page<Score> findGlobalLeaderboard(Pageable pageable);
}
