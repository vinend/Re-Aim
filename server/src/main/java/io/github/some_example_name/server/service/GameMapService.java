package io.github.some_example_name.server.service;

import io.github.some_example_name.server.model.GameMap;
import io.github.some_example_name.server.repository.GameMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing GameMap entities.
 */
@Service
public class GameMapService {
    @Autowired
    private GameMapRepository gameMapRepository;
    
    @Value("${reaim.app.uploadDir:uploads}")
    private String uploadDir;
    
    /**
     * Get all maps.
     */
    public List<GameMap> getAllMaps() {
        return gameMapRepository.findAll();
    }
    
    /**
     * Get official maps.
     */
    public List<GameMap> getOfficialMaps() {
        return gameMapRepository.findByOfficialTrue();
    }
    
    /**
     * Get community maps.
     */
    public List<GameMap> getCommunityMaps() {
        return gameMapRepository.findByOfficialFalse();
    }
    
    /**
     * Get map by ID.
     */
    public Optional<GameMap> getMapById(Long id) {
        return gameMapRepository.findById(id);
    }
    
    /**
     * Get map by name.
     */
    public Optional<GameMap> getMapByName(String name) {
        return gameMapRepository.findByName(name);
    }
    
    /**
     * Create a new map.
     */
    @Transactional
    public GameMap createMap(GameMap map) {
        return gameMapRepository.save(map);
    }
    
    /**
     * Update an existing map.
     */
    @Transactional
    public GameMap updateMap(Long id, GameMap mapDetails) {
        GameMap map = gameMapRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + id));
        
        map.setName(mapDetails.getName());
        map.setDescription(mapDetails.getDescription());
        map.setSongTitle(mapDetails.getSongTitle());
        map.setArtist(mapDetails.getArtist());
        map.setDifficultyRating(mapDetails.getDifficultyRating());
        map.setBpm(mapDetails.getBpm());
        map.setDuration(mapDetails.getDuration());
        map.setTags(mapDetails.getTags());
        
        return gameMapRepository.save(map);
    }
    
    /**
     * Delete a map by ID.
     */
    public void deleteMap(Long id) {
        gameMapRepository.deleteById(id);
    }
    
    /**
     * Upload and associate song file with a map.
     */
    @Transactional
    public GameMap uploadMapSong(Long mapId, MultipartFile songFile) throws IOException {
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        
        String fileExtension = getFileExtension(songFile.getOriginalFilename());
        String fileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Paths.get(uploadDir, "songs");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(songFile.getInputStream(), filePath);
        
        map.setSongFilename(fileName);
        return gameMapRepository.save(map);
    }
    
    /**
     * Upload and associate map data file with a map.
     */
    @Transactional
    public GameMap uploadMapData(Long mapId, MultipartFile mapDataFile) throws IOException {
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        
        String fileName = UUID.randomUUID() + ".json";
        Path uploadPath = Paths.get(uploadDir, "maps");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(mapDataFile.getInputStream(), filePath);
        
        map.setMapDataFilename(fileName);
        return gameMapRepository.save(map);
    }
    
    /**
     * Upload and associate background image with a map.
     */
    @Transactional
    public GameMap uploadBackgroundImage(Long mapId, MultipartFile imageFile) throws IOException {
        GameMap map = gameMapRepository.findById(mapId)
            .orElseThrow(() -> new RuntimeException("Map not found with id: " + mapId));
        
        String fileExtension = getFileExtension(imageFile.getOriginalFilename());
        String fileName = UUID.randomUUID() + fileExtension;
        Path uploadPath = Paths.get(uploadDir, "backgrounds");
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);
        
        map.setBackgroundImagePath(fileName);
        return gameMapRepository.save(map);
    }
    
    /**
     * Find maps by difficulty range.
     */
    public List<GameMap> findMapsByDifficultyRange(int minDifficulty, int maxDifficulty) {
        return gameMapRepository.findByDifficultyRange(minDifficulty, maxDifficulty);
    }
    
    /**
     * Find maps by tags.
     */
    public List<GameMap> findMapsByTags(List<String> tags) {
        return gameMapRepository.findByTags(tags);
    }
    
    /**
     * Find maps by artist.
     */
    public List<GameMap> findMapsByArtist(String artist) {
        return gameMapRepository.findByArtistContainingIgnoreCase(artist);
    }
    
    /**
     * Find maps by song title.
     */
    public List<GameMap> findMapsBySongTitle(String songTitle) {
        return gameMapRepository.findBySongTitleContainingIgnoreCase(songTitle);
    }
    
    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf);
    }
}
