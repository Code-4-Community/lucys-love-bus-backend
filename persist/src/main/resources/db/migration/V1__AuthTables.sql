CREATE TABLE IF NOT EXISTS blacklisted_refreshes (
    refresh_hash VARCHAR(64) PRIMARY KEY,
    expires TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(36),
    last_name VARCHAR(36),
    email VARCHAR(36) UNIQUE NOT NULL,
    pass_hash BYTEA NOT NULL,
    privilege_level INTEGER NOT NULL,
    verified INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS verification_keys (
    id      VARCHAR(50) PRIMARY KEY,
    user_id INT NOT NULL,
    used    BOOLEAN DEFAULT false,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT verification_keys_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE  IF NOT EXISTS pf_requests (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    description TEXT NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pf_requests_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id)
);
