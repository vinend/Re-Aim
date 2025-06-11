package io.github.reaim.controller;

import io.github.reaim.model.Player;
import io.github.reaim.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Player player) {
        if (playerRepository.existsByUsername(player.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        if (playerRepository.existsByEmail(player.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        Player savedPlayer = playerRepository.save(player);
        return ResponseEntity.ok(savedPlayer);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        Player player = playerRepository.findByUsername(username);
        if (player != null && player.getPassword().equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("player", player);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body("Invalid username or password");
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        Player player = playerRepository.findByUsername(username);
        if (player != null) {
            return ResponseEntity.ok(player);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Player updatedPlayer) {
        Player existingPlayer = playerRepository.findById(updatedPlayer.getId()).orElse(null);
        if (existingPlayer != null) {
            existingPlayer.setEmail(updatedPlayer.getEmail());
            existingPlayer.setPassword(updatedPlayer.getPassword());
            return ResponseEntity.ok(playerRepository.save(existingPlayer));
        }
        return ResponseEntity.notFound().build();
    }
}
