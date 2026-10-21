CREATE TABLE generation_request (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    movement_key VARCHAR(100) NOT NULL,
    draft_revision INTEGER NOT NULL,
    avatar_id VARCHAR(36) NOT NULL REFERENCES avatar (id),
    viewpoint_id VARCHAR(36) NOT NULL REFERENCES viewpoint (id),
    status VARCHAR(16) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX idx_generation_request_movement ON generation_request (movement_key);

CREATE TABLE generation_stage (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    description TEXT NOT NULL,
    reference_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (request_id, position)
);

CREATE TABLE generation_result (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (request_id, position)
);
