ALTER TABLE verification_keys
    ADD COLUMN type INT NOT NULL;

ALTER TABLE users
    DROP COLUMN verified;
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT false;
