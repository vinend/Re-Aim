package io.github.reaim.repository;

import io.github.reaim.model.Level;
import io.github.reaim.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LevelRepository extends MongoRepository<Level, String> {
    Level findByName(String name);
    List<Level> findByCreator(Player creator);
    List<Level> findByDifficulty(String difficulty);
}
