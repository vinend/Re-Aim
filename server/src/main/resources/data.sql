-- Initialize database schema for ReAim game

-- Create admin user
INSERT INTO player (username, password, email, role, created_at, updated_at) 
VALUES ('admin', '$2a$10$VlCN5bvkFTx4OYs1dVeCROZU2HsX5RXBgIjVs0aPaW9Z.XtXfXlYy', 'admin@reaim.com', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create sample game maps (beginner level)
INSERT INTO game_map (title, artist, creator, difficulty, bpm, length, tags, description, download_count, play_count, created_at, updated_at) 
VALUES 
('Tutorial Map', 'ReAim', 'System', 1.0, 120, 60, 'tutorial,beginner', 'A simple map to learn the basics', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('First Steps', 'ReAim Team', 'System', 1.5, 100, 90, 'beginner,slow', 'Practice your first aim movements', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Warming Up', 'Electronic Vibes', 'BeatMaster', 2.0, 130, 120, 'beginner,electronic', 'A gentle warm-up map', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create sample game maps (intermediate level)
INSERT INTO game_map (title, artist, creator, difficulty, bpm, length, tags, description, download_count, play_count, created_at, updated_at) 
VALUES 
('Precision Test', 'Digital Harmonic', 'AimPro', 3.5, 140, 150, 'intermediate,precision', 'Focus on accuracy with this map', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Speed Challenge', 'Rapid Beat', 'FastFingers', 4.0, 160, 180, 'intermediate,speed,challenge', 'Test your reaction time', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create sample game maps (advanced level)
INSERT INTO game_map (title, artist, creator, difficulty, bpm, length, tags, description, download_count, play_count, created_at, updated_at) 
VALUES 
('Expert Flow', 'Synthwave Masters', 'ProMapper', 4.5, 175, 210, 'advanced,flow,synthwave', 'Complex patterns for experienced players', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Master Challenge', 'Drum and Bass Collective', 'LegendaryMapper', 5.0, 180, 240, 'advanced,challenge,dnb', 'Only for the most skilled players', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: Passwords in this script are bcrypt hashed. The plaintext for admin is 'admin123'
-- In production, use stronger passwords and different database initialization approaches
