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
    specification TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

-- At most one Viewpoint is the default.
CREATE UNIQUE INDEX uq_viewpoint_single_default ON viewpoint (is_default) WHERE is_default;

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
    image_url TEXT NOT NULL,
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

-- Illustration generation: one record per order to generate a Movement's Illustrations

CREATE TABLE generation_movement_illustrations_request (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    status VARCHAR(16) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP
);

CREATE INDEX idx_generation_request_movement ON generation_movement_illustrations_request (movement_key);

-- At most one generation in progress per Movement.
CREATE UNIQUE INDEX uq_generation_request_in_progress
    ON generation_movement_illustrations_request (movement_key)
    WHERE status IN ('PENDING', 'RUNNING');

CREATE TABLE generation_illustration_stage (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_movement_illustrations_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    avatar_id VARCHAR(36) NOT NULL REFERENCES avatar (id),
    viewpoint_id VARCHAR(36) NOT NULL REFERENCES viewpoint (id),
    instructions TEXT NOT NULL,
    candidate_state VARCHAR(16) NOT NULL,
    candidate_image_url TEXT,
    candidate_failure_reason TEXT,
    candidate_progress TEXT,
    provider VARCHAR(64),
    model VARCHAR(128),
    provider_request_id VARCHAR(128),
    provider_status_url TEXT,
    provider_result_url TEXT,
    obtained_at TIMESTAMP,
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
