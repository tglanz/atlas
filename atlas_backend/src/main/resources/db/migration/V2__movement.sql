CREATE TABLE movement (
    movement_key VARCHAR(100) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE movement_cue (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    cue TEXT NOT NULL,
    PRIMARY KEY (movement_key, position)
);

CREATE TABLE movement_tag (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (movement_key, tag)
);

CREATE TABLE movement_link (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    target_key VARCHAR(100) NOT NULL,
    relation_type VARCHAR(64) NOT NULL,
    PRIMARY KEY (movement_key, position)
);

CREATE TABLE movement_illustration (
    movement_key VARCHAR(100) NOT NULL REFERENCES movement (movement_key) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (movement_key, position)
);
