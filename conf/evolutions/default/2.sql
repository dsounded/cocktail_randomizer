# Users schema

# --- !Ups
CREATE TABLE cocktails (
  alcoholic_id integer NOT NULL,
  cocktail_number integer NOT NULL,
  created_at timestamp NOT NULL
);

# --- !Downs
DROP TABLE cocktails;
