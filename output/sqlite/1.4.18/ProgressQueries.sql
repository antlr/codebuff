SELECT
    CASE WHEN InstanceID IS NULL
        THEN 'Total'
    ELSE InstanceID END InstanceID
    , SUM(OldStatus4) AS OldStatus4
    , SUM(Status0) AS Status0
    , SUM(Status1) AS Status1
    , SUM(Status2) AS Status2
    , SUM(Status3) AS Status3
    , SUM(Status4) AS Status4
    , SUM(OldStatus4 + Status0 + Status1 + Status2 + Status3 + Status4) AS InstanceTotal
FROM
    (
        SELECT
            CONVERT(VARCHAR, SSISInstanceID)             AS InstanceID
            , COUNT(CASE WHEN Status = 4 AND
                              CONVERT(DATE, LoadReportDBEndDate) <
                              CONVERT(DATE, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS OldStatus4
--, COUNT ( CASE WHEN Status = 4 AND LoadReportDBEndDate < GETDATE() THEN Status ELSE NULL END ) AS OldStatus4
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
                              DATEPART(DAY, LoadReportDBEndDate) = DATEPART(DAY, GETDATE())
                        THEN Status
                    ELSE NULL END)             AS Status4
        FROM dbo.ClientConnection
        GROUP BY SSISInstanceID
    ) AS StatusMatrix
GROUP BY InstanceID
--------------------------------


SELECT
    SourceDB
    , [Status]
    , Beta
    , SSISInstanceID AS SSISIID
    , LoadStageDBStartDate
    , LoadStageDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms, DATEDIFF(ms, LoadStageDBStartDate, LoadStageDBEndDate), 0), 114) AS StageLoadTime
    , LoadReportDBStartDate
    , LoadReportDBEndDate
    , CONVERT(VARCHAR(12), DATEADD(ms, DATEDIFF(ms, LoadReportDBStartDate, LoadReportDBEndDate), 0), 114) AS ReportLoadTime
FROM ClientConnection
GROUP BY Beta
      , Status, SSISInstanceID, SourceDB, LoadStageDBStartDate, LoadStageDBEndDate, LoadReportDBStartDate, LoadReportDBEndDate
ORDER BY Status
    ASC
    , SSISInstanceID
    ASC
, Beta
    ASC
, LoadStageDBStartDate
    ASC
--------------------------------
-- the current records from the error logging table

SELECT *
FROM DMartLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GetDate())
ORDER BY ErrorDateTime
    DESC
--------------------------------

-- works fully




SELECT
    CASE WHEN CAST(Beta AS VARCHAR) IS NULL
        THEN 'Grand Total'
    ELSE CAST(Beta AS VARCHAR) END AS Beta
    , CASE
      WHEN SourceDB IS NULL
          THEN 'Beta Group Total'
      ELSE SourceDB END AS SourceDB
--, LoadStageDBStartDate
--, LoadStageDBEndDate
    , CONVERT(VARCHAR(12),
              DATEADD(ms, SUM(DATEDIFF(ms, LoadStageDBStartDate, LoadStageDBEndDate)), 0),
              114) AS StageLoadTime
--, LoadReportDBStartDate
--, LoadReportDBEndDate
    , CONVERT(VARCHAR(12),
              DATEADD(ms, SUM(DATEDIFF(ms, LoadReportDBStartDate, LoadReportDBEndDate)), 0),
              114) AS ReportLoadTime
    , CONVERT(VARCHAR(12), DATEADD(ms,SUM((DATEDIFF(ms, LoadStageDBStartDate, LoadStageDBEndDate))) +
SUM((DATEDIFF(ms, LoadReportDBStartDate, LoadReportDBEndDate))),
                                   0), 114) AS ClientTotal
FROM ClientConnection
GROUP BY Beta, SourceDB


;