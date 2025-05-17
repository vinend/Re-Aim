package io.github.some_example_name.server.repository;

import io.github.some_example_name.server.model.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GameMap entity.
 */
@Repository
public interface GameMapRepository extends JpaRepository<GameMap, Long> {
    Optional<GameMap> findByName(String name);
    
    List<GameMap> findByOfficialTrue();
    
    List<GameMap> findByOfficialFalse();
    
    @Query("SELECT m FROM GameMap m WHERE m.difficultyRating BETWEEN :minDifficulty AND :maxDifficulty")
    List<GameMap> findByDifficultyRange(int minDifficulty, int maxDifficulty);
    
    // Search maps by tags
    @Query("SELECT DISTINCT m FROM GameMap m JOIN m.tags t WHERE t IN :tags")
    List<GameMap> findByTags(List<String> tags);
    
    // Find maps by artist
    List<GameMap> findByArtistContainingIgnoreCase(String artist);
    
    // Find maps by song title
    List<GameMap> findBySongTitleContainingIgnoreCase(String songTitle);
}
