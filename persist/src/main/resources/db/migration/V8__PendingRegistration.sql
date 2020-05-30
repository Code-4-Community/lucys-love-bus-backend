CREATE TABLE IF NOT EXISTS pending_registrations
(
    id                         serial PRIMARY KEY,
    created                    timestamp DEFAULT CURRENT_TIMESTAMP,
    user_id                    integer NOT NULL,
    event_id                   integer NOT NULL,
    ticket_quantity_delta      integer NOT NULL CHECK (ticket_quantity_delta > 0),
    stripe_checkout_session_id varchar(64),

    CONSTRAINT registration_user_fkey FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT registration_event_fkey FOREIGN KEY (event_id) REFERENCES events (id)
);
