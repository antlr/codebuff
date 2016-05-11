SELECT CAST(cast(g.name as varbinary(256)) AS sysname) AS [FileGroup_Name]
    , s.name AS [Name]
    , CAST(CASE s.file_id
WHEN 1 THEN 1
ELSE 0
END AS bit) AS [IsPrimaryFile]
    , CAST(CASE when s.growth = 0 THEN ( CASE WHEN s.type = 2 THEN 0
                                              ELSE 99
                                         END) ELSE s.is_percent_growth END AS int) AS [GrowthType]
    , s.physical_name AS [FileName]
    , s.size * CONVERT(float, 8) AS [Size]
    , CASE when s.max_size = -1 then -1 else s.max_size * CONVERT(float, 8) END AS [MaxSize]
    , s.file_id AS [ID]
    , 'Server[@Name=' + quotename(CAST(serverproperty('Servername') AS sysname), '''') + ']' + '/Database[@Name=' + quotename(db_name(), '''') + ']' + '/FileGroup[@Name=' + quotename(CAST(cast(g.name as varbinary(256)) AS sysname), '''') + ']' + '/File[@Name=' + quotename(s.name, '''') + ']' AS [Urn]
    , CAST(CASE s.is_percent_growth
        WHEN 1
          THEN s.growth
ELSE s.growth * 8 END AS float) AS [Growth]
    , s.is_media_read_only AS [IsReadOnlyMedia]
    , s.is_read_only AS [IsReadOnly]
    , CAST(case s.state
when 6 then 1
else 0
end AS bit) AS [IsOffline]
    , s.is_sparse AS [IsSparse]
FROM    sys.filegroups AS g
    INNER JOIN
    sys.master_files AS s
    ON (
       (s.type = 2
or s.type = 0) and s.database_id = db_id() and (s.drop_lsn IS NULL))
    AND (s.data_space_id = g.data_space_id)
ORDER BY [FileGroup_Name] ASC, [Name] ASC

---------------------


SELECT s.name AS [Name]
    , s.physical_name AS [FileName]
FROM    MASTER.sysdatabases AS dtb, sys.master_files AS s
    WHERE  (s.TYPE = 1
            AND s.database_id = Db_id())
AND ((dtb.name = Db_name()))
ORDER BY [Name] ASC

------------------


SELECT CONVERT(nvarchar(32), SERVERPROPERTY('Servername')) AS Server
    , '?' as DatabaseName
    , [?].sysfiles.name AS LogicalName
    , sys.master_filesphysical_name AS FileName
    , GETDATE()
        From [?].sysfiles
WHERE  DATABASEPROPERTYEX('?', 'Updateability') != 'READ_ONLY'
OR DATABASEPROPERTYEX('?', 'Status') != 'RECOVERING'
       OR DATABASEPROPERTYEX('?', 'Status') != 'RESTORING'
       OR DATABASEPROPERTYEX('?', 'Status') != 'OFFLINE'