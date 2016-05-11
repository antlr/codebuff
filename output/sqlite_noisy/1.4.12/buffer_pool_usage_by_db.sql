-- percentage of buffer pool usage by db

SELECT @total_buffer = cntr_value
FROM sys.dm_os_performance_counters
WHERE RTRIM([object_name]) LIKE '%Buffer Manager'
AND counter_name = 'Total Pages';

WITH src AS
(
    SELECT database_id, db_buffer_pages = COUNT_BIG(*)
    FROM sys.dm_os_buffer_descriptors
       --WHERE database_id BETWEEN 5 AND 32766
    WHERE  DB_NAME([database_id]) NOT IN ('master', 'model', 'msdb', 'distribution', 'tempdb')
    GROUP BY database_id
)

SELECT [db_name] = CASE[database_id]WHEN 32767 THEN 'Resource DB' ELSE DB_NAME([database_id]) END,
       db_buffer_pages, db_buffer_MB = db_buffer_pages/128, db_buffer_percent = CONVERT(DECIMAL(6, 3),db_buffer_pages * 100.0 /
@total_buffer)
FROM src
ORDER BY db_buffer_MB DESC;

------------------------------------------------------
-- specific database

WITH src AS
(
    SELECT [Object] = o.name, [Type] = o.type_desc, [Index] = COALESCE(i.name, ''), [Index_Type] = i.type_desc, p.[object_id], p.index_id, au.allocation_unit_id
    FROM   sys.partitions AS p
    INNER JOIN sys.allocation_units AS au
    ON p.hobt_id = au.container_id
           INNER JOIN sys.objects AS o
    ON p.[object_id] = o.[object_id]
           INNER JOIN sys.indexes AS i
    ON o.[object_id] = i.[object_id]
       AND p.index_id = i.index_id
    WHERE  au.[type] IN ( 1, 2, 3)
AND o.is_ms_shipped = 0
)
SELECT src.[Object],
       src.[Type]
                   , src.[Index]
                   , src.Index_Type, buffer_pages = COUNT_BIG(b.page_id), buffer_mb = COUNT_BIG(b.page_id)/128
FROM src INNER JOIN sys.dm_os_buffer_descriptors AS b
               ON src.allocation_unit_id = b.allocation_unit_id
WHERE b.database_id = DB_ID()
                   GROUP BY src.[Object], src.[Type], src.[Index], src.Index_Type
ORDER BY buffer_pages DESC;