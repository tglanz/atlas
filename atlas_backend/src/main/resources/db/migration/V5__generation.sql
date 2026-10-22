CREATE TABLE viewpoint (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    specification TEXT NOT NULL
);

CREATE TABLE generation_request (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    movement_key VARCHAR(100) NOT NULL,
    status VARCHAR(16) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX idx_generation_request_movement ON generation_request (movement_key);

-- One row per stage. kind is AVATAR (avatar_id, viewpoint_id, instructions, reference_image_url)
-- or STATIC (image_url). Unused columns hold empty strings.
CREATE TABLE generation_stage (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    kind VARCHAR(16) NOT NULL,
    avatar_id VARCHAR(36) REFERENCES avatar (id),
    viewpoint_id VARCHAR(36) REFERENCES viewpoint (id),
    instructions TEXT NOT NULL,
    reference_image_url VARCHAR(500) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (request_id, position)
);

CREATE TABLE generation_result (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (request_id, position)
);
