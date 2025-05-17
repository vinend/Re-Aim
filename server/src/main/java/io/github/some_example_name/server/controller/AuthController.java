package io.github.some_example_name.server.controller;

import io.github.some_example_name.server.model.Player;
import io.github.some_example_name.server.security.JwtResponse;
import io.github.some_example_name.server.security.JwtUtils;
import io.github.some_example_name.server.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PlayerService playerService;

    /**
     * Login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Player player = playerService.getPlayerByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Player not found with username: " + userDetails.getUsername()));

        return ResponseEntity.ok(new JwtResponse(
            jwt,
            player.getId(),
            player.getUsername(),
            player.getEmail()
        ));
    }

    /**
     * Registration endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (playerService.getPlayerByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        // Create new player account
        Player player = new Player();
        player.setUsername(registerRequest.getUsername());
        player.setEmail(registerRequest.getEmail());
        player.setPassword(registerRequest.getPassword());

        playerService.createPlayer(player);

        return ResponseEntity.ok("Player registered successfully");
    }
    
    /**
     * Check token validity.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // Extract token value from "Bearer <token>"
            String jwt = token.substring(7);
            String username = jwtUtils.extractUsername(jwt);
            
            // Check if username exists
            if (!playerService.getPlayerByUsername(username).isPresent()) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
    
    // Request and response classes as static inner classes
    
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
