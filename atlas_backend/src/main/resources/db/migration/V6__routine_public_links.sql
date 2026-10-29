-- A null token keeps a Routine private. Revoking a link clears the token.
ALTER TABLE routine ADD COLUMN share_token VARCHAR(36);
CREATE UNIQUE INDEX uq_routine_share_token ON routine (share_token);
