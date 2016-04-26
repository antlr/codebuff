WITH daysCte
(
        d
)
AS
(
    SELECT CONVERT(DATETIME, '1 January 2011') AS d -- starting date
    UNION ALL
    SELECT DATEADD(D, 1, d)
    FROM daysCte
    WHERE DATEPART(yyyy, d) <= 2012 -- stop year
)
SELECT
    d
    , DATEPART(wk, d) AS week_number
    , DATENAME(dw, d) AS day_name
    , DATENAME(m, d)  AS month_name
    , DATENAME(q, d)  AS [quarter]
FROM daysCte
