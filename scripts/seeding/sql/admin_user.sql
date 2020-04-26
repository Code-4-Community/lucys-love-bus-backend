begin;

update users set privilege_level = 2 where first_name = 'admin';

update users set verified = 1;

commit;