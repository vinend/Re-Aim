package io.github.some_example_name.server.controller;

import io.github.some_example_name.server.model.GameMap;
import io.github.some_example_name.server.service.GameMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Controller for game map-related endpoints.
 */
@RestController
@RequestMapping("/api/maps")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameMapController {
    @Autowired
    private GameMapService gameMapService;

    @Value("${reaim.app.uploadDir:uploads}")
    private String uploadDir;

    /**
     * Get all maps.
     */
    @GetMapping
    public ResponseEntity<List<GameMap>> getAllMaps() {
        List<GameMap> maps = gameMapService.getAllMaps();
        return ResponseEntity.ok(maps);
    }

    /**
     * Get map by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMapById(@PathVariable Long id) {
        return gameMapService.getMapById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get official maps (accessible without authentication).
     */
    @GetMapping("/public/official")
    public ResponseEntity<List<GameMap>> getOfficialMaps() {
        List<GameMap> maps = gameMapService.getOfficialMaps();
        return ResponseEntity.ok(maps);
    }

    /**
     * Get community maps.
     */
    @GetMapping("/community")
    public ResponseEntity<List<GameMap>> getCommunityMaps() {
        List<GameMap> maps = gameMapService.getCommunityMaps();
        return ResponseEntity.ok(maps);
    }

    /**
     * Create a new map (admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GameMap> createMap(@RequestBody GameMap map) {
        GameMap newMap = gameMapService.createMap(map);
        return ResponseEntity.ok(newMap);
    }

    /**
     * Update an existing map (admin only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMap(@PathVariable Long id, @RequestBody GameMap mapDetails) {
        return gameMapService.getMapById(id)
            .map(map -> {
                GameMap updatedMap = gameMapService.updateMap(id, mapDetails);
                return ResponseEntity.ok(updatedMap);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Upload song file for a map (admin only).
     */
    @PostMapping(value = "/{id}/song", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadMapSong(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            GameMap updatedMap = gameMapService.uploadMapSong(id, file);
            return ResponseEntity.ok(updatedMap);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload song: " + e.getMessage());
        }
    }

    /**
     * Upload map data file (admin only).
     */
    @PostMapping(value = "/{id}/data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadMapData(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            GameMap updatedMap = gameMapService.uploadMapData(id, file);
            return ResponseEntity.ok(updatedMap);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload map data: " + e.getMessage());
        }
    }

    /**
     * Upload background image for a map (admin only).
     */
    @PostMapping(value = "/{id}/background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadBackgroundImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            GameMap updatedMap = gameMapService.uploadBackgroundImage(id, file);
            return ResponseEntity.ok(updatedMap);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload background image: " + e.getMessage());
        }
    }

    /**
     * Download song file.
     */
    @GetMapping("/{id}/song")
    public ResponseEntity<Resource> downloadSong(@PathVariable Long id) {
        return gameMapService.getMapById(id)
            .map(map -> {
                try {
                    Path filePath = Paths.get(uploadDir, "songs", map.getSongFilename());
                    Resource resource = new UrlResource(filePath.toUri());
                    
                    if (resource.exists()) {
                        return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + map.getSongFilename() + "\"")
                            .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (MalformedURLException e) {
                    return ResponseEntity.badRequest().build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Download map data file.
     */
    @GetMapping("/{id}/data")
    public ResponseEntity<Resource> downloadMapData(@PathVariable Long id) {
        return gameMapService.getMapById(id)
            .map(map -> {
                try {
                    Path filePath = Paths.get(uploadDir, "maps", map.getMapDataFilename());
                    Resource resource = new UrlResource(filePath.toUri());
                    
                    if (resource.exists()) {
                        return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + map.getMapDataFilename() + "\"")
                            .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (MalformedURLException e) {
                    return ResponseEntity.badRequest().build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Download background image.
     */
    @GetMapping("/{id}/background")
    public ResponseEntity<Resource> downloadBackgroundImage(@PathVariable Long id) {
        return gameMapService.getMapById(id)
            .map(map -> {
                try {
                    Path filePath = Paths.get(uploadDir, "backgrounds", map.getBackgroundImagePath());
                    Resource resource = new UrlResource(filePath.toUri());
                    
                    if (resource.exists()) {
                        return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + map.getBackgroundImagePath() + "\"")
                            .body(resource);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                } catch (MalformedURLException e) {
                    return ResponseEntity.badRequest().build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Find maps by difficulty.
     */
    @GetMapping("/search/difficulty")
    public ResponseEntity<List<GameMap>> findMapsByDifficulty(
            @RequestParam("min") int minDifficulty,
            @RequestParam("max") int maxDifficulty) {
        
        List<GameMap> maps = gameMapService.findMapsByDifficultyRange(minDifficulty, maxDifficulty);
        return ResponseEntity.ok(maps);
    }

    /**
     * Find maps by tags.
     */
    @PostMapping("/search/tags")
    public ResponseEntity<List<GameMap>> findMapsByTags(@RequestBody Map<String, List<String>> tags) {
        List<GameMap> maps = gameMapService.findMapsByTags(tags.get("tags"));
        return ResponseEntity.ok(maps);
    }

    /**
     * Find maps by artist.
     */
    @GetMapping("/search/artist")
    public ResponseEntity<List<GameMap>> findMapsByArtist(@RequestParam("name") String artist) {
        List<GameMap> maps = gameMapService.findMapsByArtist(artist);
        return ResponseEntity.ok(maps);
    }

    /**
     * Find maps by song title.
     */
    @GetMapping("/search/song")
    public ResponseEntity<List<GameMap>> findMapsBySongTitle(@RequestParam("title") String songTitle) {
        List<GameMap> maps = gameMapService.findMapsBySongTitle(songTitle);
        return ResponseEntity.ok(maps);
    }

    /**
     * Delete a map (admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMap(@PathVariable Long id) {
        return gameMapService.getMapById(id)
            .map(map -> {
                gameMapService.deleteMap(id);
                return ResponseEntity.ok("Map deleted successfully");
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
