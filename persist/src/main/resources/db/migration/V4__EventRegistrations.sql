DROP TABLE IF EXISTS user_events;

CREATE TABLE IF NOT EXISTS event_registrations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    event_id INTEGER NOT NULL,
    ticket_quantity INTEGER NOT NULL,
    registration_status INTEGER NOT NULL,
    stripe_checkout_session_id VARCHAR(64),

    CONSTRAINT registration_user_fkey FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT registration_event_fkey FOREIGN KEY (event_id) REFERENCES events(id)
);