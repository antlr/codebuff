SELECT
    Client_ID
    , SourceDB
    , [Status]
    , Beta
    , LoadStageDBStartDate
    , LoadStageDBEndDate
    , DATEDIFF(minute, LoadStageDBStartDate, LoadStageDBEndDate) AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , DATEDIFF(minute, LoadReportDBStartDate, LoadReportDBEndDate) AS ReportLoadTime
FROM ClientConnection
WHERE Beta = '1'
ORDER BY Beta, 2

UPDATE ClientConnection
SET Beta = '0'
WHERE SourceDB IN ('AddisonAve32', 'Chevron', 'EDCO', 'ConstructionLoanCompany', 'Delta', 'Dupont', 'Kern32', 'MembersMortgage', 'Suncoast32', 'Wescom')

UPDATE ClientConnection
SET Beta = '1'
WHERE Client_ID IN ('136')
-------------------------------------------

UPDATE ClientConnection
SET LoadStageDBStartDate    = @DayAgo, LoadStageDBEndDate = @DayAgo, LoadReportDBStartDate = @DayAgo, LoadReportDBEndDate = @DayAgo, Status = 4
WHERE SourceDB = 'MembersMortgage'
----------------------------------------------
/*
TRUNCATE TABLE DMartLogging
*/

SELECT *
FROM DMartLogging
ORDER BY ErrorDateTime
    DESC
----------------------------------------------

SELECT name
FROM sys.databases
WHERE Name LIKE '%Stage%'
----------------------------------------------

SELECT name
FROM sys.databases
WHERE Name LIKE '%Data%'
----------------------------------------------

SELECT *
FROM opsinfo_ops_dbo.clients
WHERE client_name LIKE '%Merrimack%'
----------------------------------------------

SELECT
    Client_id
    , SourceServer
    , SourceDB
    , Status
    , Beta
    , StageServer
    , StageDB
    , ReportServer
    , ReportDB
    , LoadStageDBStartDate
    , LoadStageDBEndDate
    , DATEDIFF(minute, LoadStageDBStartDate, LoadStageDBEndDate) AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , DATEDIFF(minute, LoadReportDBStartDate, LoadReportDBEndDate) AS ReportLoadTime
FROM ClientConnection
--WHERE ReportServer = 'PSQLRPT22'
--WHERE SourceServer = 'STGSQL511'
--WHERE Beta = 2
ORDER BY Beta, 3
----------------

----------------------------------------------------------------

----------------


SELECT *
FROM ClientConnection
WHERE ReportServer = 'PSQLRPT22'
ORDER BY Beta, 3 --DESC
----------------


SELECT
    Client_ID
    , SourceServer
    , SourceDB
    , StageServer
    , StageDB
    , ReportServer
    , ReportDB
    , Status
    , Beta
FROM ClientConnection
WHERE ReportServer = 'PSQLRPT22'
ORDER BY Beta, 3 --DESC
----------------

UPDATE ClientConnection
SET Status = '0'
WHERE Beta = '1'

UPDATE ClientConnection
SET LoadStageDBStartDate    = @Now, LoadStageDBEndDate = @Now, Status = 2
WHERE Beta = '1'

UPDATE ClientConnection
SET ReportServer = 'PSQLRPT22'
WHERE ReportServer = 'PSLQRPT22'

---------------------------------------------

UPDATE ClientConnection
SET StageServer    = 'PSQLRPT22', ReportServer = 'PSQLRPT22', Beta = '0'
WHERE SourceDB = 'Redwood'

UPDATE ClientConnection
SET SourceServer = 'PSQLDLS30'
WHERE SourceDB = 'HiwayCU'

UPDATE ClientConnection
SET Client_ID = '228'
WHERE SourceDB = 'RLC'

SELECT
    SUM(StageLoadTime) AS TotalStageLoadTime
    , SUM(ReportLoadTime) AS TotalReportLoadTime
FROM
    (
        SELECT
--SourceServer,
            Client_ID
            , SourceDB
            , [Status]
            , Beta
            , LoadStageDBStartDate
            , LoadStageDBEndDate
            , DATEDIFF(minute, LoadStageDBStartDate, LoadStageDBEndDate)             AS StageLoadTime
            , LoadReportDBStartDate
            , LoadReportDBEndDate
            , DATEDIFF(minute, LoadReportDBStartDate, LoadReportDBEndDate)             AS ReportLoadTime
        FROM ClientConnection
        WHERE Beta != '2'
    ORDER BY Beta, 2
    ) t