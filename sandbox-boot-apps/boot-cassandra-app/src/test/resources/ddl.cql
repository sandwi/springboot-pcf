CREATE TABLE customer (id TimeUUID PRIMARY KEY, firstName text, lastName text, customerType text);
CREATE INDEX customerFistNameIndex ON customer (firstName);
CREATE INDEX customerLastNameIndex ON customer (lastName);