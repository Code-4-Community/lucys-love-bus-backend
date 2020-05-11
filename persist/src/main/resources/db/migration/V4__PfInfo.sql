CREATE TABLE IF NOT EXISTS aux_maincontact_info (

	id SERIAL PRIMARY KEY,
	user_id INT UNIQUE NOT NULL,
	address TEXT,
	city VARCHAR(32),
	state VARCHAR(2),
	zipCode VARCHAR(16),
	phoneNumber INT UNIQUE NOT NULL,

	CONSTRAINT aux_maincontact_info_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)

);

CREATE TABLE IF NOT EXISTS additional_contacts (

	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,
	phoneNumber INT NOT NULL,
	email VARCHAR(32) UNIQUE NOT NULL,
	shouldSendEmails BOOLEAN NOT NULL,
	allergies TEXT,

	CONSTRAINT additional_contacts_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)

);

CREATE TABLE IF NOT EXISTS children (

	id SERIAL PRIMARY KEY,
	user_id INT NOT NULL,
	date_of_birth DATE,
	pronouns VARCHAR(56),
	shool_year VARCHAR(56),
	school VARCHAR(56),
	alleriges TEXT,
	diagnosis TEXT,
	medications TEXT,
	notes TEXT

	CONSTRAINT children_users_id_fk FOREIGN KEY (user_id) REFERENCES users(id)

);