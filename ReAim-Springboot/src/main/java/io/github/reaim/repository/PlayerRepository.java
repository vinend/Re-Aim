package io.github.reaim.repository;

import io.github.reaim.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    Player findByUsername(String username);
    Player findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
