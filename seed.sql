delete from users;
delete from courses;
delete from ratings;

-- USER SEEDS

insert into
  users (email, first_name, last_name, password)
values
  ('test1@test.com', 'jon', 'smith', 123);

insert into
  users (email, first_name, last_name, password)
values
  ('test2@test.com', 'abe', 'andrews', 123);

insert into
  users (email, first_name, last_name, password)
values
  ('test3@test.com', 'vincent', 'bevins', 123);

-- COURSE SEEDS

insert into
  courses (name, description, instructor_id, category, rate)
values (
  'spaghetti', 'make spaghetti',
(select id from users where first_name = 'jon')
,'Cooking', 3);

insert into
  courses (name, description, instructor_id, category, rate)
values (
  'dslr class', 'make pictures', 
  ( select id from users where first_name = 'jon' )
  , 'Photography/Film', 3);


-- RATING SEEDS

insert into
  ratings (user_id, course_id, rating, comment) 
values (
  (select id from users where first_name = 'jon'),
  (select id from courses where name = 'spaghetti'),
  1,
  'terrible!'
  );

insert into
  ratings (user_id, course_id, rating, comment) 
values (
  (select id from users where first_name = 'jon'),
  (select id from courses where name = 'dslr class'),
  4,
  'Great class by Jon!'
  )