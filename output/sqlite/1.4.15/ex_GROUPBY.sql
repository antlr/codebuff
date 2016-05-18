INSERT INTO girl (name, hair, did_date) VALUES ('Azure', 'Brunette', 0);

INSERT INTO girl (name, hair, did_date) VALUES ('Sarah', 'Brunette', 1);

INSERT INTO girl (name, hair, did_date) VALUES ('Ashley', 'Brunette', 1);

INSERT INTO girl (name, hair, did_date) VALUES ('Heather', 'Blonde', 1);

-- query 1

SELECT
    g.hair
    , (COUNT(*)) AS girl_count
FROM girl g
GROUP BY g.hair

-- not so optimal query

SELECT
    g.hair
    , g.did_date
    , (COUNT(*)) AS girl_count
FROM girl g
GROUP BY g.hair, g.did_date

-- badass NULLIF query
-- http://www.bennadel.com/blog/579-SQL-COUNT-NULLIF-Is-Totally-Awesome.htm

SELECT
    g.hair
    , (COUNT(*)) AS girl_count
    , (COUNT(NULLIF(did_date, 0))) AS did_date_count
    , (COUNT(NULLIF(did_date, 1))) AS did_not_date_count
FROM girl g
GROUP BY g.hair


-- badass CASE query
-- http://www.bennadel.com/blog/582-SQL-Aggregates-Support-CASE-Statements.htm

SELECT
    g.hair
    , (COUNT(*)) AS girl_count
    , (COUNT(CASE WHEN did_date = 1
                 THEN did_date
             ELSE NULL END)) AS did_date_count
    , (COUNT(CASE WHEN did_date = 0
                 THEN did_date
             ELSE NULL END)) AS did_not_date_count
FROM girl g
GROUP BY g.hair