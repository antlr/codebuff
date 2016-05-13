insert into t values ('AA',150,100,50,4)
insert into t values ('HHH',161,125,36,4)
insert into t values ('PPPP',160,85,75,4)
insert into t values ('JJJJJ',120,56,64,2)
insert into t values ('GGGG',40,31,9,2)

SELECT  CASE WHEN Col IS NULL THEN 'Total' ELSE Col END COL
    , SUM(COL1) AS Col1
    , SUM(COL2) AS COL2
    , SUM(COL3) AS COL3
    , SUM(COL4) AS COL4
FROM t
GROUP BY COL
ORDER BY CASE WHEN Col IS NULL THEN 999 ELSE 0 END