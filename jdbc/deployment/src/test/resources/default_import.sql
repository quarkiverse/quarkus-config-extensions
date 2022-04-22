CREATE TABLE IF NOT EXISTS configuration (configuration_key varchar(255), configuration_value varchar(255));

INSERT INTO configuration (configuration_key, configuration_value)
VALUES ('greeting.message', 'hello from default table');
