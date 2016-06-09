SELECT
    SourceDB
    , [Status]
    , Beta
    , SSISInstanceID AS SSISIID
    , LoadStageDBStartDate
    , LoadStageDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms,
                                   DATEDIFF(ms, LoadStageDBStartDate,
                                            LoadStageDBEndDate), 0),
              114)   AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms,
                                   DATEDIFF(ms, LoadReportDBStartDate,
                                            LoadReportDBEndDate), 0),
              114)   AS ReportLoadTime
FROM ClientConnection
GROUP BY Beta, Status, SSISInstanceID, SourceDB, LoadStageDBStartDate,
    LoadStageDBEndDate, LoadReportDBStartDate, LoadReportDBEndDate
ORDER BY Status
    ASC
    , SSISInstanceID
    ASC
    , Beta
    ASC
    , LoadStageDBStartDate
    ASC

-------------------------------------------
SELECT
    ClientID
    , Status
    , Beta
    , SourceServer
    , SourceDB
    , CDCReportServer
    , CDCReportDB
    , LSNPrevious
    , LSNMax
    , LSNFromPrevious
    , LSNFrom
    , StartTimeExtract
    , EndTimeExtract
    , LoanCurrentStartDate
    , LoanCurrentEndDate
    , LoanMasterStartDate
    , LoanMasterEndDate
    , LoanSecondaryStartDate
    , LoanSecondaryEndDate
FROM ClientConnection_test
ORDER BY Beta, SourceDB

UPDATE ClientConnection_test
SET Status = 0


UPDATE ClientConnection_test
SET Beta = '0'
--WHERE Beta = '1'
WHERE SourceDB IN ('PADemoDU', 'RLC', 'EliLillyFCU', 'GTE32')

SELECT
    ClientID
    , Status
    , Beta
    , SourceServer
    , SourceDB
    , CDCReportServer
    , CDCReportDB
    , LSNPrevious
    , LSNMax
    , LSNFromPrevious
    , LSNFrom
    , StartTimeExtract
    , EndTimeExtract
    , LoanCurrentStartDate
    , LoanCurrentEndDate
    , LoanMasterStartDate
    , LoanMasterEndDate
    , LoanSecondaryStartDate
    , LoanSecondaryEndDate
FROM ClientConnection_test
ORDER BY Beta, SourceDB


UPDATE ClientConnection
SET Beta = '1', SSISInstanceID = 0
WHERE SourceDB = 'PADemoDU'
-------------------------------------------
-- Sum of clients in each status by Beta number
SELECT
      CASE WHEN SSISInstanceID IS NULL
          THEN 'Total'
      ELSE SSISInstanceID END SSISInstanceID
    , SUM(OldStatus4) AS      OldStatus4
    , SUM(Status0)    AS      Status0
    , SUM(Status1)    AS      Status1
    , SUM(Status2)    AS      Status2
    , SUM(Status3)    AS      Status3
    , SUM(Status4)    AS      Status4
    , SUM(OldStatus4 + Status0 + Status1 + Status2 + Status3 + Status4)    AS      InstanceTotal
FROM
    (
        SELECT
              CONVERT(VARCHAR, SSISInstanceID) AS SSISInstanceID
            , COUNT(CASE WHEN Status = 4 AND
                              CONVERT(DATE, LoadReportDBEndDate) <
                              CONVERT(DATE, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS OldStatus4
            , COUNT(CASE WHEN Status = 0
                        THEN Status
                    ELSE NULL END)             AS Status0
            , COUNT(CASE WHEN Status = 1
                        THEN Status
                    ELSE NULL END)             AS Status1
            , COUNT(CASE WHEN Status = 2
                        THEN Status
                    ELSE NULL END)             AS Status2
            , COUNT(CASE WHEN Status = 3
                        THEN Status
                    ELSE NULL END)             AS Status3
--, COUNT ( CASE WHEN Status = 4 THEN Status ELSE NULL END ) AS Status4
            , COUNT(CASE WHEN Status = 4 AND
                              DATEPART(DAY, LoadReportDBEndDate) =
                              DATEPART(DAY, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS Status4
        FROM dbo.ClientConnection
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY SSISInstanceID
----------------------------------------------
SELECT
      CASE WHEN SSISInstanceID IS NULL
          THEN 'Total'
      ELSE SSISInstanceID END SSISInstanceID
    , SUM(OldStatus4) AS      OldStatus4
    , SUM(Status0)    AS      Status0
    , SUM(Status1)    AS      Status1
    , SUM(Status2)    AS      Status2
    , SUM(Status3)    AS      Status3
    , SUM(Status4)    AS      Status4
    , SUM(OldStatus4 + Status0 + Status1 + Status2 + Status3 + Status4)    AS      InstanceTotal
FROM
    (
        SELECT
              CONVERT(VARCHAR, SSISInstanceID) AS SSISInstanceID
            , COUNT(CASE WHEN Status = 4 AND
                              CONVERT(DATE, LoadReportDBEndDate) <
                              CONVERT(DATE, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS OldStatus4
            , COUNT(CASE WHEN Status = 0
                        THEN Status
                    ELSE NULL END)             AS Status0
            , COUNT(CASE WHEN Status = 1
                        THEN Status
                    ELSE NULL END)             AS Status1
            , COUNT(CASE WHEN Status = 2
                        THEN Status
                    ELSE NULL END)             AS Status2
            , COUNT(CASE WHEN Status = 3
                        THEN Status
                    ELSE NULL END)             AS Status3
            , COUNT(CASE WHEN Status = 4 AND
                              DATEPART(DAY, LoadReportDBEndDate) =
                              DATEPART(DAY, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS Status4
        FROM dbo.ClientConnection
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY SSISInstanceID

SELECT *
FROM DMartLogging
ORDER BY ErrorDateTime
    DESC
----------------------------------------------
SELECT *
FROM DMartLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GetDate())
ORDER BY ErrorDateTime
    DESC
----------------------------------------------
SELECT *
FROM dbo.DMartComponentLogging
ORDER BY ErrorDateTime
    ASC
----------------------------------------------
SELECT *
FROM dbo.DMartComponentLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GetDate())
      AND TaskName = 'Data Flow Task br_liability'
GROUP BY TaskName, ErrorDateTime, PackageName, DestDB, DestServer, SourceDB,
    SourceServer, ID, ClientId, ErrorMessage
ORDER BY ErrorDateTime
    ASC
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
WHERE client_name LIKE '%Eli%'


----------------------------------------------------------------
SELECT *
FROM ClientConnection
WHERE Beta != '2'
ORDER BY 3 --DESC

----------------------------------------------------------------
----------------
SELECT *
FROM ClientConnection
WHERE ReportServer = 'PSQLRPT22'
ORDER BY Beta, 3
----------------
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
    , DATEDIFF(minute, LoadStageDBStartDate,
               LoadStageDBEndDate)  AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , DATEDIFF(minute, LoadReportDBStartDate,
               LoadReportDBEndDate) AS ReportLoadTime
FROM ClientConnection
ORDER BY Beta, 3
----------------


UPDATE ClientConnection
SET LoadStageDBStartDate = @Now, LoadStageDBEndDate = @Now, Status = 2
WHERE Beta = '1'

UPDATE ClientConnection
SET ReportServer = 'PSQLRPT22'
WHERE ReportServer = 'PSLQRPT22'
---------------------------------------------
UPDATE ClientConnection
SET LoadStageDBStartDate    = '2010-03-09 01:10:33.200'
    , LoadStageDBEndDate    = '2010-03-09 01:15:20.393'
    , LoadReportDBStartDate = '2010-03-09 02:55:12.807'
    , LoadReportDBEndDate   = '2010-03-09 02:59:33.627'
WHERE Beta = '0'

UPDATE ClientConnection_test
SET SourceServer      = 'IAPPBO510'
    , CDCReportServer = 'IAPPBO510'
    , Beta            = '1'
WHERE SourceDB = 'PADemoDU'

UPDATE ClientConnection
SET LoadStageDBStartDate    = '2010-03-09 01:10:33.200'
    , LoadStageDBEndDate    = '2010-03-09 01:15:20.393'
    , LoadReportDBStartDate = '2010-03-09 02:55:12.807'
    , LoadReportDBEndDate   = '2010-03-09 02:59:33.627'
    , Status                = '0'
WHERE Beta = '0'

UPDATE dbo.ClientConnection_test
SET SourceServer      = 'STGSQL613'
    , StageDB         = 'DMart_PADemoLP_Stage'
    , CDCReportDB     = 'DMart_CDCTest_Data'
    , SourceDB        = 'PADemoLP2'
    , ClientID        = '10024'
    , StageServer     = 'STGSQLDOC710'
    , CDCReportServer = 'STGSQLDOC710'


UPDATE ClientConnection_test
SET Beta = 1
WHERE CDCReportDB = 'Dmart_GTE32_Data'

UPDATE ClientConnection_test
SET CDCReportDB = 'Dmart_GTECDC_Data'
WHERE CDCReportDB = 'Dmart_GTE32CDC_Data'


UPDATE ClientConnection
SET SSISInstanceID = 3
WHERE SourceDB = 'GTE32'

SELECT
    ClientID
    , Status
    , Beta
    , SourceServer
    , SourceDB
    , CDCReportServer
    , CDCReportDB
    , LSNPrevious
    , LSNMax
    , LSNFromPrevious
    , LSNFrom
    , StartTimeExtract
    , EndTimeExtract
    , LoanCurrentStartDate
    , LoanCurrentEndDate
    , LoanMasterStartDate
    , LoanMasterEndDate
    , LoanSecondaryStartDate
    , LoanSecondaryEndDate
FROM ClientConnection_test
ORDER BY Beta, SourceDB


UPDATE dbo.ClientConnection_test
SET LoanCurrentStartDate     = '2013-04-21 14:39:24.897'
    , LoanCurrentEndDate     = '2013-04-26 14:39:24.897'
    , LoanMasterStartDate    = '2013-04-21 14:39:24.897'
    , LoanMasterEndDate      = '2013-04-26 14:39:24.897'
    , LoanSecondaryStartDate = '2013-04-21 14:39:24.897'
    , LoanSecondaryEndDate   = '2013-04-26 14:39:24.897'


