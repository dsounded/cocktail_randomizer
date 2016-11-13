# Users schema

# --- !Ups
CREATE TABLE users (
  id serial PRIMARY KEY,
  name varchar(255) NOT NULL,
  created_at timestamp NOT NULL,
  UNIQUE (name)
);

# --- !Downs
DROP TABLE users;
