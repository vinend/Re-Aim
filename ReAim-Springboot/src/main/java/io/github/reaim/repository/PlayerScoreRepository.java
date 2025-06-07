package io.github.reaim.repository;

import io.github.reaim.model.Level;
import io.github.reaim.model.Player;
import io.github.reaim.model.PlayerScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerScoreRepository extends MongoRepository<PlayerScore, String> {
    List<PlayerScore> findByPlayer(Player player);
    List<PlayerScore> findByLevel(Level level);
    List<PlayerScore> findByPlayerAndLevel(Player player, Level level);
    List<PlayerScore> findByPlayerOrderByScoreDesc(Player player);
}
