CREATE TABLE movement_revision (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    movement_key VARCHAR(100) NOT NULL,
    revision INTEGER NOT NULL,
    status VARCHAR(16) NOT NULL,
    published_at TIMESTAMP,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    CONSTRAINT uq_movement_revision UNIQUE (movement_key, revision)
);

CREATE UNIQUE INDEX uq_movement_single_draft ON movement_revision (movement_key) WHERE status = 'DRAFT';

CREATE TABLE movement_cue (
    revision_id VARCHAR(36) NOT NULL REFERENCES movement_revision (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    cue TEXT NOT NULL,
    PRIMARY KEY (revision_id, position)
);

CREATE TABLE movement_tag (
    revision_id VARCHAR(36) NOT NULL REFERENCES movement_revision (id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (revision_id, tag)
);

CREATE TABLE movement_link (
    revision_id VARCHAR(36) NOT NULL REFERENCES movement_revision (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    target_key VARCHAR(100) NOT NULL,
    relation_type VARCHAR(64) NOT NULL,
    PRIMARY KEY (revision_id, position)
);

CREATE TABLE movement_illustration (
    revision_id VARCHAR(36) NOT NULL REFERENCES movement_revision (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    avatar_id VARCHAR(36) NOT NULL REFERENCES avatar (id),
    url VARCHAR(500) NOT NULL,
    PRIMARY KEY (revision_id, position)
);
