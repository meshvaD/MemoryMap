BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Table 1" (
	"Name"	TEXT,
	"Address"	TEXT
);
INSERT INTO "Table 1" VALUES ('John','Toronto');
INSERT INTO "Table 1" VALUES ('Jane','Brampton');
COMMIT;
