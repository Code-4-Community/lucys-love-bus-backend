ALTER TABLE children ADD COLUMN profile_picture VARCHAR(500) DEFAULT null;
ALTER TABLE contacts
RENAME COLUMN profilepicture TO profile_picture;