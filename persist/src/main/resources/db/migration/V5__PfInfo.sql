ALTER TABLE users
    ADD COLUMN address TEXT;
ALTER TABLE users
	ADD COLUMN city VARCHAR(32);
ALTER TABLE users
	ADD COLUMN state VARCHAR(32);
ALTER TABLE users
	ADD COLUMN zipCode VARCHAR(16);

ALTER TABLE users
    DROP COLUMN first_name;
ALTER TABLE users
    DROP COLUMN last_name;

CREATE TABLE IF NOT EXISTS contacts (
	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,
	is_main_contact BOOLEAN NOT NULL DEFAULT false,

    email VARCHAR(36) NOT NULL,
    should_send_emails BOOLEAN NOT NULL DEFAULT false,

    first_name VARCHAR(36) NOT NULL,
    last_name VARCHAR(36) NOT NULL,

    date_of_birth TIMESTAMP,
	phone_number VARCHAR(16),
    pronouns VARCHAR(36),
	allergies TEXT,
	diagnosis TEXT,
	medications TEXT,
	notes TEXT,

	CONSTRAINT contacts_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS children (
	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,

    first_name VARCHAR(36) NOT NULL,
    last_name VARCHAR(36) NOT NULL,
    date_of_birth TIMESTAMP NOT NULL,
	pronouns VARCHAR(32) NOT NULL,

	school_year VARCHAR(32),
	school VARCHAR(64),

	allergies TEXT,
	diagnosis TEXT,
	medications TEXT,
	notes TEXT,

	CONSTRAINT children_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)
);
