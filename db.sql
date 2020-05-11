drop table if exists roles, categories, users, contact_methods, contact_info, courses, ratings, user_courses;

create table categories(
    name varchar(128) primary key
    );

insert into categories (name) values ('Cooking');
insert into categories (name) values ('Arts & Crafts');
insert into categories (name) values ('Coding');
insert into categories (name) values ('Music');
insert into categories (name) values ('Business');
insert into categories (name) values ('Photography/Film');
insert into categories (name) values ('UI/UX Design');
insert into categories (name) values ('Fashion');
insert into categories (name) values ('Writing');


create table contact_methods(
    name varchar(128) primary key
);

insert into contact_methods (name) values ('E-Mail');
insert into contact_methods (name) values ('Discord');
insert into contact_methods (name) values ('Facebook');
insert into contact_methods (name) values ('Skype');
insert into contact_methods (name) values ('Phone');
insert into contact_methods (name) values ('Signal');

create table roles(
    name varchar(128) primary key
);

insert into roles (name) values ('Normal');
insert into roles (name) values ('Moderator');
insert into roles (name) values ('Admin');

create table users(
    id serial primary key,
    email varchar(256) not null unique,
    password varchar(64) not null,
    first_name varchar(32) not null,
    last_name varchar(32) not null,
    description varchar(1024),
    is_instructor boolean default false,
    role varchar(128) references roles(name) on delete cascade not null default 'Normal' 
);

create table courses(
    id serial primary key,
    name varchar(64) not null,
    description varchar(1024) not null,
    instructor_id integer references users(id) on delete cascade not null,
    category varchar(128) references categories(name) on delete cascade not null,
    rate real not null check(rate > 0)
);

create table contact_info(
    id serial primary key,
    user_id integer references users(id)  on delete cascade not null,
    contact_method varchar(128) references contact_methods(name) on delete cascade not null,
    contact_info varchar(256) not null
);

create table ratings(
    user_id integer references users(id) on delete cascade not null,
    course_id integer references courses(id) on delete cascade not null,
    rating smallint not null check(rating >= 0 and rating < 6),
    comment varchar(512),
    primary key (user_id, course_id)
);

create table user_courses(
    id serial primary key,
    user_id integer references courses(id) on delete cascade not null,
    course_id integer references courses(id) on delete cascade not null,
    course_date date not null,
    course_time time not null,
    course_length smallint not null  
);