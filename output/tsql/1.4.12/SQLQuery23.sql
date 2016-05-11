SELECT
    SourceDB
--, SourceServer
--, ID
--, DestServer
    , DestDB
    , PackageName
    , TaskName
--, ErrorMessage
--, ErrorDateTime
    , (
SELECT ErrorDateTime
WHERE ErrorMessage LIKE '%Validation phase is beginning.%')    AS TaskStartTime
    , (
SELECT ErrorDateTime
WHERE ErrorMessage LIKE '%Cleanup phase is beginning.%')    AS TaskEndTime
    , CONVERT(NVARCHAR(12), DATEADD(ms,
                                    DATEDIFF(ms, (
                                                 SELECT ErrorDateTime
                                                 WHERE ErrorMessage LIKE '%Validation phase is beginning.%'),
                                             (
                                             SELECT ErrorDateTime
                                             WHERE ErrorMessage LIKE '%Cleanup phase is beginning.%')), 0),
              114)    AS TaskTimeTaken
--, *
FROM dbo.DMartComponentLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GetDate())
      AND ErrorMessage NOT LIKE '%Pre-Execute phase is beginning.  %'
      AND ErrorMessage NOT LIKE '%Prepare for Execute phase is beginning.  %'
      AND ErrorMessage NOT LIKE '%The final commit for the data insertion%'
      AND ErrorMessage NOT LIKE '%The buffer manager detected%'
      AND ErrorMessage NOT LIKE '%Post Execute phase is beginning.%'
      AND ErrorMessage NOT LIKE '%Execute phase is beginning.%'
      AND ErrorMessage NOT LIKE '%component%wrote%'
ORDER BY SourceDB, TaskName, ErrorDateTime
                 ASC
-----------------------



SELECT
    SourceServer
    , SourceDB
    , DestServer
    , DestDB
    , PackageName
    , TaskName
    , ErrorMessage
    , ErrorDateTime
FROM dbo.DMartComponentLogging
WHERE DATEPART(day, ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, ErrorDateTime) = DATEPART(year, GetDate())
      AND ErrorMessage NOT LIKE '%Pre-Execute phase is beginning.  %'
      AND ErrorMessage NOT LIKE '%Prepare for Execute phase is beginning.  %'
      AND ErrorMessage NOT LIKE '%The final commit for the data insertion%'
      AND ErrorMessage NOT LIKE '%The buffer manager detected%'
      AND ErrorMessage NOT LIKE '%Post Execute phase is beginning.%'
      AND ErrorMessage NOT LIKE '%Execute phase is beginning.%'
      AND ErrorMessage NOT LIKE '%component%wrote%'
--AND TaskName = 'Data Flow Task br_liability'
GROUP BY SourceServer, SourceDB, DestServer, DestDB, TaskName, PackageName, ErrorDateTime, ErrorMessage


-----------------------------------------------------------------------------

SELECT DISTINCT
    dmcl.ClientID
    , dmcl.SourceServer
    , dmcl.SourceDB
    , dmcl.DestServer
    , dmcl.DestDB
    , dmcl.PackageName
    , dmcl.TaskName
    , dmcl.ErrorMessage
    , dmcl.ErrorDateTime
    , (
SELECT dmcl.ErrorDateTime
WHERE dmcl.ErrorMessage LIKE '%Validation phase is beginning.%')    AS TaskStartTime
--, (SELECT dmcl2.ErrorDateTime WHERE dmcl2.ErrorMessage LIKE '%Cleanup phase is beginning.%' AND dmcl.ClientID = dmcl2.ClientID AND dmcl.TaskName = dmcl2.TaskName AND dmcl.ErrorMessage LIKE '%Validation phase is beginning.%')  AS TaskEndTime
--, CONVERT(NVARCHAR(12), DATEADD(ms, DATEDIFF(ms, (SELECT dmcl.ErrorDateTime WHERE dmcl.ErrorMessage LIKE '%Validation phase is beginning.%'), (SELECT dmcl2.ErrorDateTime WHERE dmcl2.ErrorMessage LIKE '%Cleanup phase is beginning.%')), 0), 114) AS TaskTimeTaken
FROM dbo.DMartComponentLogging dmcl
    JOIN dbo.DMartComponentLogging dmcl2
        ON dmcl.TaskName = dmcl2.TaskName AND
           dmcl.SourceServer = dmcl2.SourceServer AND
           dmcl.ClientID = dmcl2.ClientID
WHERE DATEPART(day, dmcl.ErrorDateTime) = DATEPART(day, GetDate())
      AND DATEPART(month, dmcl.ErrorDateTime) = DATEPART(month, GetDate())
      AND DATEPART(year, dmcl.ErrorDateTime) = DATEPART(year, GetDate())
      AND dmcl.ErrorMessage NOT LIKE '%Pre-Execute phase is beginning.  %'
      AND dmcl.ErrorMessage NOT LIKE '%Prepare for Execute phase is beginning.  %'
      AND dmcl.ErrorMessage NOT LIKE '%The final commit for the data insertion%'
      AND dmcl.ErrorMessage NOT LIKE '%The buffer manager detected%'
      AND dmcl.ErrorMessage NOT LIKE '%Post Execute phase is beginning.%'
      AND dmcl.ErrorMessage NOT LIKE '%Execute phase is beginning.%'
      AND dmcl.ErrorMessage NOT LIKE '%component%wrote%'
GROUP BY dmcl.ClientID, dmcl.SourceServer, dmcl.SourceDB, dmcl.DestServer, dmcl.DestDB, dmcl.TaskName, dmcl.PackageName, dmcl.ErrorDateTime, dmcl2.ErrorDateTime, dmcl.ErrorMessage, dmcl2.ErrorMessage, dmcl2.ClientID, dmcl2.TaskName