SELECT *
FROM sprocswithservernames
ORDER BY server, DatabaseName

SELECT DISTINCT server
FROM sprocswithservernames
ORDER BY server
-------------------------------------------------------------------

SELECT DISTINCT
    Server
    , DatabaseName
    , ProcedureName
    , ProcedureText
FROM
    (
        SELECT *
        FROM sprocswithservernames
        WHERE DatabaseName NOT LIKE '%Status%'
                                    AND DatabaseName NOT LIKE '%PerfTest%'
    ) AS q
ORDER BY ProcedureName, DatabaseName

SELECT DISTINCT
    Server
    , DatabaseName
    , ProcedureName
    , ProcedureText
FROM
    (
        SELECT *
        FROM sprocswithservernames
        WHERE DatabaseName LIKE '%Status%'
    ) AS q
ORDER BY ProcedureName, DatabaseName
-------------------------------------------------------------------

SELECT DISTINCT ProcedureName
FROM
    (
        SELECT *
        FROM sprocswithservernames
        WHERE DatabaseName LIKE '%Status%'
    ) AS q
ORDER BY ProcedureName