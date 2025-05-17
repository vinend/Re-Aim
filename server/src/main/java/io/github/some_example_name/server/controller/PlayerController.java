package io.github.some_example_name.server.controller;

import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for player-related endpoints.
 */
@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    /**
     * Get all players.
     */
    @GetMapping
    public ResponseEntity<List<Player>> getAllPlayers() {
        List<Player> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    /**
     * Get player by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id)
            .map(player -> {
                // Don't expose sensitive info
                player.setPassword(null);
                return ResponseEntity.ok(player);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get current logged-in player.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentPlayer(@AuthenticationPrincipal UserDetails userDetails) {
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .map(player -> {
                // Don't expose sensitive info
                player.setPassword(null);
                return ResponseEntity.ok(player);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update player details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlayer(
            @PathVariable Long id,
            @RequestBody Player playerDetails,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Ensure player can only update their own profile
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .filter(player -> player.getId().equals(id))
            .map(player -> {
                Player updatedPlayer = playerService.updatePlayer(id, playerDetails);
                updatedPlayer.setPassword(null); // Don't expose password
                return ResponseEntity.ok(updatedPlayer);
            })
            .orElse(ResponseEntity.status(403).body("Access denied"));
    }

    /**
     * Update player profile picture.
     */
    @PostMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Ensure player can only update their own profile picture
        return playerService.getPlayerByUsername(userDetails.getUsername())
            .filter(player -> player.getId().equals(id))
            .map(player -> {
                try {
                    Player updatedPlayer = playerService.updateProfilePicture(id, file);
                    updatedPlayer.setPassword(null); // Don't expose password
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Profile picture updated successfully");
                    response.put("hasCustomProfile", true);
                    
                    return ResponseEntity.ok(response);
                } catch (IOException e) {
                    return ResponseEntity.badRequest().body("Failed to upload profile picture: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.status(403).body("Access denied"));
    }

    /**
     * Get player profile picture.
     */
    @GetMapping(value = "/{id}/profile-picture", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getProfilePicture(@PathVariable Long id) {
        return playerService.getPlayerById(id)
            .map(player -> {
                if (player.getProfilePicture() != null && player.isHasCustomProfile()) {
                    return ResponseEntity.ok(player.getProfilePicture());
                } else {
                    // No custom profile picture
                    return ResponseEntity.notFound().build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete player account (admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        return playerService.getPlayerById(id)
            .map(player -> {
                playerService.deletePlayer(id);
                return ResponseEntity.ok("Player deleted successfully");
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
