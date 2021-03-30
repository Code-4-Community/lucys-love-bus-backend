UPDATE announcements
    SET title = SUBSTRING(title from 1 for 255);

ALTER TABLE announcements
    ALTER COLUMN title
    TYPE VARCHAR(255);

ALTER TABLE announcements
    ALTER COLUMN description
    TYPE TEXT;

ALTER TABLE children
    ALTER COLUMN first_name
    TYPE VARCHAR(255);

ALTER TABLE children
    ALTER COLUMN last_name
    TYPE VARCHAR(255);

ALTER TABLE children
    ALTER COLUMN pronouns
    TYPE VARCHAR(255);

ALTER TABLE children
    ALTER COLUMN school_year
    TYPE VARCHAR(50);

ALTER TABLE children
    ALTER COLUMN school
    TYPE VARCHAR(255);

ALTER TABLE contacts
    ALTER COLUMN email
    TYPE VARCHAR(255);

ALTER TABLE contacts
    ALTER COLUMN first_name
    TYPE VARCHAR(255);

ALTER TABLE contacts
    ALTER COLUMN last_name
    TYPE VARCHAR(255);

ALTER TABLE contacts
    ALTER COLUMN pronouns
    TYPE VARCHAR(255);

ALTER TABLE events
    ALTER COLUMN title
    TYPE VARCHAR(255);

ALTER TABLE events
    ALTER COLUMN location
    TYPE VARCHAR(255);

ALTER TABLE users
    ALTER COLUMN email
    TYPE VARCHAR(255);

ALTER TABLE users
    ALTER COLUMN city
    TYPE VARCHAR(255);

-- TODO: should this be a 2 letter code?
ALTER TABLE users
    ALTER COLUMN state
    TYPE VARCHAR(255);

ALTER TABLE users
    ALTER COLUMN zipcode
    TYPE VARCHAR(25);

ALTER TABLE announcements
    ALTER COLUMN image_src
    TYPE TEXT;

ALTER TABLE children
    ALTER COLUMN profile_picture
    TYPE TEXT;

ALTER TABLE contacts
    ALTER COLUMN profile_picture
    TYPE TEXT;

ALTER TABLE events
    ALTER COLUMN thumbnail
    TYPE TEXT;

-- TODO: is contacts.phone_number not going to have a standardized length? can this
--  include [()-.] characters?
