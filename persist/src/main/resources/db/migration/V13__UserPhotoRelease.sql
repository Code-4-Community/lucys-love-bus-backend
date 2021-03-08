ALTER TABLE users
ADD photo_release BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET photo_release = true WHERE TRUE;