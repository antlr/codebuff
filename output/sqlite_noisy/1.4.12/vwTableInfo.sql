/*
    vwTableInfo - Table Information View

 This view display space and storage information for every table in a
SQL Server 2005 database.
Columns are:
    Schema
    Name
    Owner       (may be different from Schema)
    Columns     (count of the max number of columns ever used)
    HasClusIdx  1 if table has a clustered index, 0 otherwise
    RowCount
    IndexKB     space used by the table's indexes
    DataKB      space used by the table's data

 16-March-2008, RBarryYoung@gmail.com
 31-January-2009, Edited for better formatting
*/
--CREATE VIEW vwTableInfo
-- AS
SELECT SCHEMA_NAME(tbl.schema_id) as [Schema],
        tbl.Name
                                              , Coalesce((Select pr.name
                                                          From sys.database_principals pr
                                                          Where pr.principal_id = tbl.principal_id), SCHEMA_NAME(tbl.schema_id)) as [Owner]
                                              , tbl.max_column_id_used as [Columns]
                                              , CAST(CASE idx.index_id WHEN 1 THEN 1 ELSE 0 END AS bit) AS [HasClusIdx]
                                              , Coalesce((Select sum(spart.rows)
                                                          from sys.partitions spart
                                                          Where spart.object_id = tbl.object_id and spart.index_id < 2), 0) AS [RowCount]
                                              , Coalesce((
Select Cast(v.low/1024.0 as float) * SUM(a.used_pages -CASE WHEN a.type <> 1 THEN a.used_pages WHEN p.index_id < 2 THEN a.data_pages ELSE 0 END)
FROM   sys.indexes as i
      JOIN sys.partitions as p
ON p.object_id = i.object_id and p.index_id = i.index_id
      JOIN sys.allocation_units as a
ON a.container_id = p.partition_id
Where i.object_id = tbl.object_id), 0.0) AS [IndexKB]
                                              , Coalesce((
Select Cast(v.low/1024.0 as float) * SUM( CASE WHEN a.type <> 1 THEN a.used_pages WHEN p.index_id < 2 THEN a.data_pages ELSE 0 END )
FROM   sys.indexes as i
      JOIN sys.partitions as p
ON p.object_id = i.object_id and p.index_id = i.index_id
      JOIN sys.allocation_units as a
ON a.container_id = p.partition_id
Where i.object_id = tbl.object_id), 0.0) AS [DataKB]
                                              , tbl.create_date
                                              , tbl.modify_date
FROM   sys.tables AS tbl
INNER JOIN
sys.indexes AS idx
ON(idx.object_id = tbl.object_id and idx.index_id < 2)
INNER JOIN
master_dbo.spt_values v
ON(v.number = 1
    and v.type = 'E')