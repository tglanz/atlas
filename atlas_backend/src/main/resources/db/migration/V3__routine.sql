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
