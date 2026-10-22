-- The product's own data: movements and the graph between them, illustrations and how they are
-- generated, and routines.

-- Illustration generation presets

CREATE TABLE avatar (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    specification TEXT NOT NULL
);

CREATE TABLE viewpoint (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    specification TEXT NOT NULL
);

-- Movements

CREATE TABLE movement (
    movement_key VARCHAR(100) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE movement_item (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (movement_key, position)
);

CREATE TABLE movement_tag (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (movement_key, tag)
);

CREATE TABLE movement_illustration (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (movement_key, position)
);

-- The movement graph: directed, typed edges between Movements. They live outside the Movement.

CREATE TABLE movement_edge (
    source_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    target_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    edge_type VARCHAR(64) NOT NULL,
    PRIMARY KEY (source_key, target_key, edge_type)
);

CREATE INDEX idx_movement_edge_target ON movement_edge (target_key);

-- Illustration generation requests

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

-- Routines

CREATE TABLE routine (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    owner_id VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL
);

CREATE INDEX idx_routine_owner ON routine (owner_id);

CREATE TABLE routine_section (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    routine_id VARCHAR(36) NOT NULL REFERENCES routine (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE routine_section_movement (
    section_id VARCHAR(36) NOT NULL REFERENCES routine_section (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key),
    PRIMARY KEY (section_id, position)
);
