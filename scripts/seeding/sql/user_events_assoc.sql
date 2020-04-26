begin;

insert into user_events (select users.id as user_id, events.id as event_id from users, events);
	
commit;
