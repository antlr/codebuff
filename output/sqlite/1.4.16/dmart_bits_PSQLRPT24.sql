SELECT
    SourceServer
    , SourceDB
    , [Status]
    , Beta
    , SSISInstanceID AS SSISIID
    , LoadStageDBStartDate
    , LoadStageDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms, DATEDIFF(ms, LoadStageDBStartDate, LoadStageDBEndDate),
                                   0),
              114)    AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms, DATEDIFF(ms, LoadReportDBStartDate, LoadReportDBEndDate),
                                   0),
              114)    AS ReportLoadTime
FROM ClientConnection
GROUP BY Beta, Status, SSISInstanceID, SourceDB, LoadStageDBStartDate, LoadStageDBEndDate, LoadReportDBStartDate, LoadReportDBEndDate, SourceServer
ORDER BY SourceDB, Status
                 ASC
                 , Beta
                 ASC
                 , SSISInstanceID
                 ASC
                 , LoadStageDBStartDate
                 ASC
                 , SourceServer
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

----------------------------------------------

SELECT *
FROM DMartLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GETDATE())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GETDATE())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GETDATE())
ORDER BY ErrorDateTime
    DESC
----------------------------------------------




SELECT
    CASE WHEN SSISInstanceID IS NULL
        THEN 'Total'
    ELSE SSISInstanceID END SSISInstanceID
    , SUM(Status0)    AS Status0
    , SUM(Status1)    AS Status1
    , SUM(Status2)    AS Status2
    , SUM(Status3)    AS Status3
    , SUM(Status4)    AS Status4
FROM
    (
        SELECT
            CONVERT(VARCHAR, SSISInstanceID) AS SSISInstanceID
            , COUNT(CASE WHEN Status = 0
            THEN Status
                    ELSE NULL END) AS Status0
            , COUNT(CASE WHEN Status = 1
            THEN Status
                    ELSE NULL END) AS Status1
            , COUNT(CASE WHEN Status = 2
            THEN Status
                    ELSE NULL END) AS Status2
            , COUNT(CASE WHEN Status = 3
            THEN Status
                    ELSE NULL END) AS Status3
            , COUNT(CASE WHEN Status = 4
            THEN Status
                    ELSE NULL END) AS Status4
        FROM dbo.ClientConnectionCDC
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY SSISInstanceID
----------------------------------------------
-- Sum of clients in each status by Beta number


SELECT
    CASE WHEN SSISInstanceID IS NULL
        THEN 'Total'
    ELSE SSISInstanceID END SSISInstanceID
    , SUM(Status0)    AS Status0
    , SUM(Status1)    AS Status1
    , SUM(Status2)    AS Status2
    , SUM(Status3)    AS Status3
    , SUM(Status4)    AS Status4
FROM
    (
        SELECT
            CONVERT(VARCHAR, SSISInstanceID) AS SSISInstanceID
            , COUNT(CASE WHEN Status = 0
            THEN Status
                    ELSE NULL END) AS Status0
            , COUNT(CASE WHEN Status = 1
            THEN Status
                    ELSE NULL END) AS Status1
            , COUNT(CASE WHEN Status = 2
            THEN Status
                    ELSE NULL END) AS Status2
            , COUNT(CASE WHEN Status = 3
            THEN Status
                    ELSE NULL END) AS Status3
            , COUNT(CASE WHEN Status = 4
            THEN Status
                    ELSE NULL END) AS Status4
        FROM dbo.ClientConnection
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY SSISInstanceID
----------------------------------------------


SELECT
    CASE WHEN InstanceID IS NULL
        THEN 'Total'
    ELSE InstanceID END InstanceID
    , SUM(OldStatus4)    AS OldStatus4
    , SUM(Status0)    AS Status0
    , SUM(Status1)    AS Status1
    , SUM(Status2)    AS Status2
    , SUM(Status3)    AS Status3
    , SUM(Status4)    AS Status4
    , SUM(OldStatus4 + Status0 + Status1 + Status2 + Status3 + Status4)    AS InstanceTotal
FROM
    (
        SELECT
            CONVERT(VARCHAR, SSISInstanceID) AS InstanceID
            , COUNT(CASE WHEN Status = 4 AND
                              CONVERT(DATE, EndTimeExtract) <
                              CONVERT(DATE, GETDATE())
            THEN Status
                    ELSE NULL END) AS OldStatus4
            , COUNT(CASE WHEN Status = 0
            THEN Status
                    ELSE NULL END) AS Status0
            , COUNT(CASE WHEN Status = 1
            THEN Status
                    ELSE NULL END) AS Status1
            , COUNT(CASE WHEN Status = 2
            THEN Status
                    ELSE NULL END) AS Status2
            , COUNT(CASE WHEN Status = 3
            THEN Status
                    ELSE NULL END) AS Status3
            , COUNT(CASE WHEN Status = 4 AND DATEPART(DAY, EndTimeExtract) = DATEPART(DAY, GETDATE())
            THEN Status
                    ELSE NULL END) AS Status4
        FROM dbo.ClientConnectionCDC
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY InstanceID
----------------------------------------------


SELECT
    CASE WHEN InstanceID IS NULL
        THEN 'Total'
    ELSE InstanceID END InstanceID
    , SUM(OldStatus4)    AS OldStatus4
    , SUM(Status0)    AS Status0
    , SUM(Status1)    AS Status1
    , SUM(Status2)    AS Status2
    , SUM(Status3)    AS Status3
    , SUM(Status4)    AS Status4
    , SUM(OldStatus4 + Status0 + Status1 + Status2 + Status3 + Status4)    AS InstanceTotal
FROM
    (
        SELECT
            CONVERT(VARCHAR, SSISInstanceID) AS InstanceID
            , COUNT(CASE WHEN Status = 4 AND
                              CONVERT(DATE, LoadReportDBEndDate) <
                              CONVERT(DATE, GETDATE())
            THEN Status
                    ELSE NULL END) AS OldStatus4
            , COUNT(CASE WHEN Status = 0
            THEN Status
                    ELSE NULL END) AS Status0
            , COUNT(CASE WHEN Status = 1
            THEN Status
                    ELSE NULL END) AS Status1
            , COUNT(CASE WHEN Status = 2
            THEN Status
                    ELSE NULL END) AS Status2
            , COUNT(CASE WHEN Status = 3
            THEN Status
                    ELSE NULL END) AS Status3
            , COUNT(CASE WHEN Status = 4 AND DATEPART(DAY, LoadReportDBEndDate) = DATEPART(DAY, GETDATE())
            THEN Status
                    ELSE NULL END) AS Status4
        FROM dbo.ClientConnection
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY InstanceID;

SELECT *
FROM DMartLogging
WHERE TaskName NOT LIKE '%Kill%Active%'
ORDER BY ErrorDateTime
    DESC

SELECT *
FROM DMartLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GETDATE())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GETDATE())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GETDATE())
ORDER BY ErrorDateTime
    DESC
-------------------------------------------

SELECT *
FROM dbo.DMartComponentLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GETDATE())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GETDATE())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GETDATE())
GROUP BY TaskName, ErrorDateTime, PackageName, DestDB, DestServer, SourceDB, SourceServer, ID, ClientId, ErrorMessage
ORDER BY ErrorDateTime
    DESC
-------------------------------------------
-------------------------------------------

UPDATE ClientConnection
SET LoadStageDBStartDate = '@3AM', LoadStageDBEndDate = '@3AM', LoadReportDBStartDate = '@3AM', LoadReportDBEndDate = '@3AM', Status = 4
WHERE Beta = '1'
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
    , CONVERT(varchar(12), DATEADD(ms, DATEDIFF(ms, LoadStageDBStartDate, LoadStageDBEndDate),
                                   0),
              114)    AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , CONVERT(varchar(12), DATEADD(ms, DATEDIFF(ms, LoadReportDBStartDate, LoadReportDBEndDate),
                                   0),
              114)    AS ReportLoadTime
FROM ClientConnection
ORDER BY Beta, 3
----------------------------------------------
-- EliLillyFCU STGSQL615 10086
-- PeoplesBank STGSQL613 10090
-- Solarity STGSQL615 10091

SELECT *
FROM OPSINFO_ops_dbo.clients
WHERE client_name LIKE '%Royal%'

UPDATE dbo.ClientConnectionCDC
SET CDCReportDB = 'Dmart_OrangeCountyCDC_Data'
WHERE CDCReportDB = 'Dmart_OrangeCounty32CDC_Data'

----------------------------------------------
----------------------------------------------

SELECT
    SUM(StageLoadTime)    AS TotalStageLoadTime
    , SUM(ReportLoadTime)    AS TotalReportLoadTime
FROM
    (
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
        WHERE Beta != '2'
    ) AS t
----------------------------------------------

SELECT SUM(DataLoadTime)    AS TotalStageLoadTime
FROM
    (
        SELECT
            ClientID
            , [dbo].[ClientConnectionCDC].[CDCExtractDB]
            , [Status]
            , Beta
            , [dbo].[ClientConnectionCDC].[StartTimeExtract]
            , [dbo].[ClientConnectionCDC].[EndTimeExtract]
            , DATEDIFF(minute, StartTimeExtract, EndTimeExtract) AS DataLoadTime
         --, LoadReportDBStartDate
         --, LoadReportDBEndDate
         --, DATEDIFF(minute, LoadReportDBStartDate,
         --           LoadReportDBEndDate) AS ReportLoadTime
        FROM ClientConnectionCDC
        WHERE Status != 0
    ) AS t
----------------------------------------------

SELECT *
FROM clientconnection
ORDER BY ssisinstanceid


UPDATE dbo.ClientConnection
SET SSISinstanceID = CASE SourceServer
                          WHEN 'PSQLDLS30'
                          THEN 0
                          WHEN 'PSQLDLS31'
                          THEN 1
                          WHEN 'PSQLDLS32'
                          THEN 2
                          WHEN 'PSQLDLS33'
                          THEN 3
                          WHEN 'PSQLDLS34'
                          THEN 4
                          WHEN 'PSQLDLS35'
                          THEN 5
                          WHEN 'PSQLRPT24'
                          THEN 5 -- DMart Template
                     END

SELECT *
FROM dbo.ClientConnection
WHERE SourceDB LIKE 'Royal%'

UPDATE dbo.ClientConnection
SET Beta = 0
WHERE SourceDB = 'RoyalCU'