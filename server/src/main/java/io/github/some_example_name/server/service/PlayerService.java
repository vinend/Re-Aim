package io.github.some_example_name.server.service;

import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Player entities.
 */
@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get all players.
     */
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    /**
     * Get player by ID.
     */
    public Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }
    
    /**
     * Get player by username.
     */
    public Optional<Player> getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username);
    }
    
    /**
     * Create a new player.
     */
    public Player createPlayer(Player player) {
        // Encode the password before saving
        player.setPassword(passwordEncoder.encode(player.getPassword()));
        return playerRepository.save(player);
    }
    
    /**
     * Update an existing player.
     */
    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        
        player.setEmail(playerDetails.getEmail());
        // Only update password if provided
        if (playerDetails.getPassword() != null && !playerDetails.getPassword().isEmpty()) {
            player.setPassword(passwordEncoder.encode(playerDetails.getPassword()));
        }
        
        return playerRepository.save(player);
    }
    
    /**
     * Delete a player by ID.
     */
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
    
    /**
     * Update player's profile picture.
     */
    @Transactional
    public Player updateProfilePicture(Long id, MultipartFile profilePicture) throws IOException {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        
        player.setProfilePicture(profilePicture.getBytes());
        player.setHasCustomProfile(true);
        
        return playerRepository.save(player);
    }
    
    /**
     * Update player stats after a game.
     */
    @Transactional
    public void updatePlayerStats(Long id, int shotsAdded, int hitsAdded, long playtimeAdded) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        
        player.setGamesPlayed(player.getGamesPlayed() + 1);
        player.setTotalShots(player.getTotalShots() + shotsAdded);
        player.setTotalHits(player.getTotalHits() + hitsAdded);
        player.setTotalPlaytime(player.getTotalPlaytime() + playtimeAdded);
        
        playerRepository.save(player);
    }
}
