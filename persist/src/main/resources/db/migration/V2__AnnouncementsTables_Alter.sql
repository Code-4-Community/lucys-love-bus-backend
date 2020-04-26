ALTER TABLE announcements ADD event_id int DEFAULT NULL;
ALTER TABLE announcements ADD CONSTRAINT event_fkey FOREIGN KEY (event_id) REFERENCES events(id);
