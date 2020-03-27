CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    title VARCHAR(36) NOT NULL,
    description TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    location VARCHAR(36) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_events (
    users_id INTEGER NOT NULL,
    event_id INTEGER NOT NULL,
    PRIMARY KEY (users_id, event_id),

    CONSTRAINT users_event_fkey FOREIGN KEY (users_id) REFERENCES users(id),
    CONSTRAINT event_users_fkey FOREIGN KEY (event_id) REFERENCES events(id)
)
