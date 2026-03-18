CREATE TABLE IF NOT EXISTS json_config (a varchar(255), b varchar(255));

INSERT INTO json_config (a, b)
VALUES ('greeting', '{"message":"hello from json table"}');

INSERT INTO json_config (a, b)
VALUES ('appuser', '{"name":"John Doe","age":"30","city":"New York","active":"true","email":"john.doe@example.com"}');
