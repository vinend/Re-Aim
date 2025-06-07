-- Player Table
CREATE TABLE player (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Level Table
CREATE TABLE level (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    json_file TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    creator_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (creator_id) REFERENCES player(id)
);

-- PlayerScore Table
CREATE TABLE player_score (
    id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    level_id VARCHAR(36) NOT NULL,
    score INT NOT NULL,
    player_level INT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player(id),
    FOREIGN KEY (level_id) REFERENCES level(id)
);

-- Indexes
CREATE INDEX idx_player_username ON player(username);
CREATE INDEX idx_player_email ON player(email);
CREATE INDEX idx_level_creator ON level(creator_id);
CREATE INDEX idx_player_score_player ON player_score(player_id);
CREATE INDEX idx_player_score_level ON player_score(level_id);
