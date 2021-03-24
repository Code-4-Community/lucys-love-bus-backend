UPDATE announcements
    SET title = SUBSTRING(title from 1 for 255);

UPDATE announcements
    SET description = SUBSTRING(title from 1 for 255);

ALTER TABLE announcements
    ALTER COLUMN title
    TYPE VARCHAR(255);

ALTER TABLE announcements
    ALTER COLUMN description
    TYPE VARCHAR(255);

ALTER TABLE children
    ALTER COLUMN first_name
    TYPE VARCHAR(255);

ALTER TABLE children
    ALTER COLUMN last_name
    TYPE VARCHAR(255);

-- TODO: do we want more/less space for pronouns?
ALTER TABLE children
    ALTER COLUMN pronouns
    TYPE VARCHAR(75);

-- TODO: what is a school year? something like "Spring 2020"/"August-May"/...? Do we need
--  more space for this or can we do it in less?
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
    TYPE VARCHAR(75);

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

-- TODO: do we want extended zip code here? shouldn't this just be an int?
ALTER TABLE users
    ALTER COLUMN zipcode
    TYPE VARCHAR(5);

-- TODO: not gonna touch thumbnail/image_src/profile_picture varchars, but are we
--  storing image bytes in db or url? if URL, is 500 long enough?

-- TODO: is contacts.phone_number not going to have a standardized length? does this enforceably
--  include [()-.] characters? if not, why not make it an int?
