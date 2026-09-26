ALTER TABLE realms ADD COLUMN members BOOLEAN NOT NULL DEFAULT 0;

UPDATE realms SET members = 1 WHERE name = 'dev';
