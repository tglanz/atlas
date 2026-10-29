-- Capture a request independently of later movement or preset edits.
ALTER TABLE generation_movement_illustrations_request ADD COLUMN input_name VARCHAR(200);
ALTER TABLE generation_movement_illustrations_request ADD COLUMN input_description TEXT;
ALTER TABLE generation_movement_illustrations_request ADD COLUMN guidelines TEXT;
ALTER TABLE generation_movement_illustrations_request ADD COLUMN avatar_specification TEXT;
ALTER TABLE generation_movement_illustrations_request ADD COLUMN viewpoint_specification TEXT;
ALTER TABLE generation_movement_illustrations_request ADD COLUMN stage_fingerprint VARCHAR(64);
ALTER TABLE generation_movement_illustrations_request ADD COLUMN target_stage_position INTEGER;
CREATE TABLE generation_input_cue (
    request_id VARCHAR(36) NOT NULL REFERENCES generation_movement_illustrations_request (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (request_id, position)
);

-- A partially illustrated movement may have gaps in its stage coverage.
ALTER TABLE movement_illustration ADD COLUMN stage_position INTEGER;
UPDATE movement_illustration SET stage_position = position;
