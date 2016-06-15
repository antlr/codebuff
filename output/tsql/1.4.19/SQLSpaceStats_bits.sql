SELECT *
FROM SQLSpaceStats a
    JOIN
    (
        SELECT
            Server_Name
            , dbname
            , flag
            , FileID
            , FileGroup
            , total_space
            , usedspace
            , freespace
            , freepct
            , Name
            , [FileName]
            , LastUpdate
            , max(seq_num)             AS max_seq_num
        FROM SQLSpaceStats
        GROUP BY Server_Name
        , dbname
        , flag
        , FileID
        , FileGroup
        , total_space
        , usedspace
        , freespace
        , freepct
        , Name
        , LastUpdate
        , [FileName]
        HAVING count(*) > 1
    ) b
        ON a.Server_Name = b.Server_Name AND
           a.dbname = b.dbname AND
           a.flag = b.flag AND
           a.FileID = b.FileID AND
           a.FileGroup = b.FileGroup AND
           a.total_space = b.total_space AND
           a.usedspace = b.usedspace AND
           a.freespace = b.freespace AND
           a.freepct = b.freepct AND
           a.Name = b.Name AND
           a.[FileName] = b.[FileName] AND
           a.LastUpdate = b.LastUpdate AND
           a.seq_num < b.max_seq_num

----------------------------------------------------------

SELECT
    server_name
    , ServerID
    , dbname
    , LastUpdate
    , flag
    , Fileid
    , FileGroup
    , total_space
    , freespace
    , freepct
    , [FileName]
FROM SQLSpaceStats
--where server_name IN ('HARTFORD','STGSQL610','STGSQL611','STGSQLCBS620','STGSQLDOC710','STGSQLMET620') --= '9999'
--where server_name = 'STGSQLDOC710'
--WHERE LastUpdate > GetDate() - 2
--AND server_name = 'STGSQL611'
GROUP BY LastUpdate
    , dbname
    , server_name
    , ServerID
    , flag
    , Fileid
    , FileGroup
    , total_space
    , freespace
    , freepct
    , [FileName]
ORDER BY dbname, LastUpdate
    DESC
-----------------------------------------------------------

SELECT *
FROM SQLSpaceStats
WHERE DATEPART(day, lastupdate) = DATEPART(day, GetDate())
      AND DATEPART(month, LastUpdate) = DATEPART(month, GETDATE())
      AND DATEPART(year, LastUpdate) = DATEPART(year, GETDATE())
ORDER BY dbname

----------------------------------------------------------

DELETE FROM SQLSpaceStats
WHERE ServerID = '9999'
----------------------------------------------------------

DELETE FROM SQLSpaceStats
WHERE lastUpdate = '2011-07-13 10:19:33.363'