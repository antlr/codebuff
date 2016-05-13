select * from sprocswithservernames
order by server, DatabaseName

select distinct server
from sprocswithservernames
order by server
-------------------------------------------------------------------

SELECT DISTINCT Server,
                DatabaseName,
                ProcedureName,
                ProcedureText
FROM
(
select * from sprocswithservernames
WHERE DatabaseName NOT LIKE '%Status%' AND DatabaseName NOT LIKE '%PerfTest%'
) AS q
ORDER BY ProcedureName, DatabaseName

SELECT DISTINCT Server,
                DatabaseName,
                ProcedureName,
                ProcedureText
FROM
(
select * from sprocswithservernames
WHERE DatabaseName LIKE '%Status%'
) AS q
ORDER BY ProcedureName, DatabaseName
-------------------------------------------------------------------

SELECT DISTINCT ProcedureName
FROM
(
select * from sprocswithservernames
WHERE DatabaseName LIKE '%Status%'
) AS q
ORDER BY ProcedureName