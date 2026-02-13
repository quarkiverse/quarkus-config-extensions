CREATE TABLE IF NOT EXISTS json_config (a varchar(255), b varchar(255));

INSERT INTO json_config (a, b)
VALUES ('greeting', '{"message":"hello from json table"}');
