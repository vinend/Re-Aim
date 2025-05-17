-- Database schema definition for ReAim game

-- Player table stores user accounts and authentication info
CREATE TABLE IF NOT EXISTS player (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    profile_picture VARCHAR(255),
    total_play_count INT DEFAULT 0,
    total_hits INT DEFAULT 0,
    total_misses INT DEFAULT 0,
    highest_combo INT DEFAULT 0,
    average_accuracy DOUBLE DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Game map table stores information about available maps and songs
CREATE TABLE IF NOT EXISTS game_map (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    creator VARCHAR(50) NOT NULL,
    difficulty DOUBLE NOT NULL,
    bpm INT NOT NULL,
    length INT NOT NULL,
    tags VARCHAR(255),
    description TEXT,
    map_file_path VARCHAR(255),
    song_file_path VARCHAR(255),
    background_image_path VARCHAR(255),
    download_count INT DEFAULT 0,
    play_count INT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Score table records player performance on maps
CREATE TABLE IF NOT EXISTS score (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL,
    map_id BIGINT NOT NULL,
    score INT NOT NULL,
    accuracy DOUBLE NOT NULL,
    hits INT NOT NULL,
    misses INT NOT NULL,
    combo INT NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES player(id),
    FOREIGN KEY (map_id) REFERENCES game_map(id)
);

-- Create indices for better query performance
CREATE INDEX IF NOT EXISTS idx_score_player_id ON score(player_id);
CREATE INDEX IF NOT EXISTS idx_score_map_id ON score(map_id);
CREATE INDEX IF NOT EXISTS idx_score_score ON score(score DESC);
CREATE INDEX IF NOT EXISTS idx_game_map_difficulty ON game_map(difficulty);
CREATE INDEX IF NOT EXISTS idx_player_username ON player(username);
