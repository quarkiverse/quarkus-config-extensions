
CREATE TABLE IF NOT EXISTS configuration (key varchar(255), value varchar(255));

INSERT INTO configuration (key, value)
VALUES ('greeting.message', 'hello from default table');
