CREATE TABLE avatar (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    specification TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_avatar_single_default ON avatar (is_default) WHERE is_default;

CREATE TABLE viewpoint (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    specification TEXT NOT NULL
);
