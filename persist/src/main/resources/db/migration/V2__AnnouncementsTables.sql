CREATE TABLE IF NOT EXISTS announcements (
    id serial PRIMARY KEY,
    title varchar NOT NULL,
    description varchar NOT NULL,
    created timestamp DEFAULT CURRENT_TIMESTAMP
);
