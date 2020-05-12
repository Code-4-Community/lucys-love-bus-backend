ALTER TABLE users
    ADD COLUMN address TEXT;
ALTER TABLE users
	ADD COLUMN city VARCHAR(32);
ALTER TABLE users
	ADD COLUMN state VARCHAR(32);
ALTER TABLE users
	ADD COLUMN zipCode VARCHAR(16);
ALTER TABLE users
    ADD COLUMN phoneNumber varchar(16) NOT NULL;
ALTER TABLE users
	ADD COLUMN allergies TEXT;

CREATE TABLE IF NOT EXISTS additional_contacts (
	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,

	phoneNumber VARCHAR(16) NOT NULL,
	email VARCHAR(36) NOT NULL,
	shouldSendEmails BOOLEAN NOT NULL,

	allergies TEXT,

	CONSTRAINT additional_contacts_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS children (
	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,

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
