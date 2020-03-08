CREATE TABLE announcements (
    id int NOT NULL PRIMARY KEY,
    title varchar NOT NULL,
    description varchar NOT NULL,
    created timestamp DEFAULT CURRENT_TIMESTAMP
);
