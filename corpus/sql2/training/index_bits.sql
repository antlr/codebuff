-- remove duplicates from SQLErrorLogs table
ALTER TABLE SQLIndexRebuilds
    ADD seq_num INT identity

SELECT *
FROM SQLIndexRebuilds a JOIN
( SELECT ServerName
, DBName
, SQLStatement
, IndexType
, FragPercent
, max(seq_num) AS max_seq_num
FROM SQLIndexRebuilds
GROUP BY ServerName, DBName, SQLStatement, IndexType, FragPercent
HAVING count(*) > 1) b
ON a.ServerName = b.ServerName AND
a.DBName = b.DBName AND
a.SQLStatement = b.SQLStatement AND
a.IndexType = b.IndexType AND
a.FragPercent = b.FragPercent AND
a.seq_num < b.max_seq_num

--------------------------------------------------------------------------
SELECT *
FROM SQLIndexRebuilds
ORDER BY LastUpdate
    DESC
--------------------------------------------------------------------------
SELECT 'dbcc showcontig (' +
       CONVERT(varchar(20), i.id) + ',' + -- table id
       CONVERT(varchar(20), i.indid) + ') -- ' + -- index id
       object_name(i.id) + '.' + -- table name
       i.name -- index name
FROM sysobjects o
    INNER JOIN sysindexes i
        ON (o.id = i.id)
WHERE o.type = 'U'
      AND i.indid < 2
      AND
      i.id = object_id(o.name)
ORDER BY
    object_name(i.id), i.indid

--------------------------------------------------------------------------
--Script to identify table fragmentation

--Set the table and index to be examined
SELECT @IndexName = 'index_name' --enter name of index

--Get the Index Values
SELECT @IndexID = IndID
FROM sysindexes
WHERE id = @ID AND name = @IndexName

--------------------------------------------------------------------------
-- show "missing" indexes
--------------------------------------------------------------------------
-- system views for indices
SELECT *
FROM sys.dm_db_missing_index_details
SELECT *
FROM sys.dm_db_missing_index_groups
SELECT *
FROM sys.dm_db_missing_index_group_stats
--------------------------------------------------------------------------
SELECT *
FROM sys.dm_db_missing_index_details mid
    JOIN sys.dm_db_missing_index_groups mig
        ON mid.index_handle = mig.index_handle
WHERE Statement LIKE '%BECU%'
      OR Statement LIKE '%WrightPatt%'
ORDER BY Statement
--------------------------------------------------------------------------
SELECT
      db_name(d.database_id)   dbname
    , object_name(d.object_id) tablename
    , d.index_handle
    , d.equality_columns
    , d.inequality_columns
    , d.included_columns
    , d.statement AS           fully_qualified_object
    , gs.*
FROM sys.dm_db_missing_index_groups g
    JOIN sys.dm_db_missing_index_group_stats gs
        ON gs.group_handle = g.index_group_handle
    JOIN sys.dm_db_missing_index_details d ON g.index_handle = d.index_handle
WHERE d.database_id = d.database_id AND d.object_id = d.object_id
ORDER BY dbname
--ORDER BY gs.user_seeks DESC
--   and object_name(d.object_id) = 'Address'
--------------------------------------------------------------------------
-- show statistics update date
--------------------------------------------------------------------------
SELECT
      o.name                                AS TableName
    , i.name                                AS IndexName
    , i.type_desc                           AS IndexType
    , STATS_DATE(i.[object_id], i.index_id) AS StatisticsDate
FROM
    sys.indexes i
    JOIN sys.objects o ON i.[object_id] = o.[object_id]
WHERE
    o.type = 'U'     --Only get indexes for User Created Tables
    AND i.name IS NOT NULL
ORDER BY
    o.name, i.type
--------------------------------------------------------------------------
-- show unused indices
--------------------------------------------------------------------------
SELECT
    OBJECT_NAME(sys.indexes.object_id) TableName
    , sys.indexes.name
    , sys.dm_db_index_usage_stats.user_seeks
    , sys.dm_db_index_usage_stats.user_scans
    , sys.dm_db_index_usage_stats.user_lookups
    , sys.dm_db_index_usage_stats.user_updates
FROM sys.dm_db_index_usage_stats
    JOIN sys.indexes
        ON sys.dm_db_index_usage_stats.object_id = sys.indexes.object_id
           AND sys.dm_db_index_usage_stats.index_id = sys.indexes.index_id
           AND sys.indexes.name NOT LIKE 'PK%'
           AND OBJECT_NAME(sys.indexes.object_id) <> 'sysdiagrams'
WHERE sys.dm_db_index_usage_stats.database_id = DB_ID()
      AND user_scans = 0
      AND user_scans = 0
      AND user_lookups = 0
      AND user_seeks = 0
      AND sys.dm_db_index_usage_stats.index_id NOT IN (0, 1)
ORDER BY OBJECT_NAME(sys.indexes.object_id)
    , sys.indexes.name


SELECT *
FROM SQLIndexRebuilds
ORDER BY TimesRebuilt
    DESC



