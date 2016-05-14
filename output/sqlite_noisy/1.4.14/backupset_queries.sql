---------------------------------------------------------------------------------
--Database Backups for all databases For Previous Week
---------------------------------------------------------------------------------
SELECT CONVERT(CHAR(100),SERVERPROPERTY('Servername')) AS Server
    , msdb_dbo_backupset_database_name
    , msdb_dbo_backupset_backup_start_date
    , msdb_dbo_backupset_backup_finish_date
    , msdb_dbo_backupset_expiration_date
    , CASE msdb_backupset.type WHEN 'D' THEN 'Database' WHEN 'L' THEN 'Log'
      END AS backup_type
    , msdb_dbo_backupset_backup_size
    , msdb_dbo_backupmediafamily.logical_device_name
    , msdb_dbo_backupmediafamily.physical_device_name
    , msdb_dbo_backupset_name AS backupset_name
    , msdb_dbo_backupset_description
FROM msdb_dbo_backupmediafamily INNER
    JOIN msdb_dbo_backupset
    ON msdb_dbg_backupmediafamily_media_set_id = msdb_dbo_backupset_media_set_id
WHERE (CONVERT(datetime,msdb_dbo_backupset_backup_start_date, 102)>= GETDATE() -7)
ORDER BY msdb_dbo_backupset_database_name, msdb_dbo_backupset_backup_finish_date
-------------------------------------------------------------------------------------------
--Most Recent Database Backup for Each Database - Detailed
-------------------------------------------------------------------------------------------    

SELECT  A.[Server]
    , A.database_name
    , B.backup_start_date
    , B.backup_finish_date
    , CONVERT(varchar(12),DATEADD(ms,
                                  DATEDIFF(ms,B.backup_start_date,B.backup_finish_date), 0), 114) AS BackupTime
    , B.backup_size
    --, B.expiration_date
    --, B.logical_device_name
    --, B.physical_device_name
    --, B.backupset_name
FROM (
   SELECT CONVERT(CHAR(100),SERVERPROPERTY('Servername')) AS Server, msdb_dbo_backupset_database_name, MAX (msdb_dbo_backupset_backup_finish_date) AS last_db_backup_date
   FROM   msdb_dbg_backupmediafamily INNER
JOIN msdb_dbo_backupset
ON msdb_dbg_backupmediafamily_media_set_id = msdb_dbo_backupset_media_set_id
                                                                                                     WHERE  msdb_backupset_type = 'D'
                                                                                                         GROUP BY msdb_dbo_backupset_database_name
) AS A
LEFT JOIN
    (
SELECT CONVERT(CHAR(100),SERVERPROPERTY('Servername')) AS Server
    , msdb_dbo_backupset_database_name
    , msdb_dbo_backupset_backup_start_date
    , msdb_dbo_backupset_backup_finish_date
    , msdb_dbo_backupset_expiration_date
    , msdb_dbo_backupset_backup_size
    , msdb_dbg_backupmediafamily.logical_device_name
    , msdb_dbg_backupmediafamily.physical_device_name
    , msdb_dbo_backupset_name AS backupset_name
    , msdb_dbo_backupset_description
        FROM   msdb_dbg_backupmediafamily INNER
JOIN msdb_dbo_backupset
ON msdb_dbg_backupmediafamily_media_set_id = msdb_dbo_backupset_media_set_id
WHERE  msdb_backupset_type = 'D') AS B
    ON A.[server] = B.[server] AND A.[database_name] = B.[database_name] AND A.[last_db_backup_date] = B.[backup_finish_date]
ORDER BY backup_finish_date
-------------------------------------------------------------------------------------------
--Databases with data backup over 24 hours old

SELECT  CONVERT(CHAR(100),SERVERPROPERTY('Servername')) AS Server
    , msdb_dbo_backupset_database_name
    , MAX (msdb_dbo_backupset_backup_finish_date) AS last_db_backup_date
    , DATEDIFF(hh,
MAX(msdb_dbo_backupset_backup_finish_date),
GETDATE()) AS [Backup Age (Hours)]
FROM msdb_dbo_backupset
    WHERE msdb_dbo_backupset_type = 'D'
    GROUP BY msdb_dbo_backupset_database_name HAVING (MAX(msdb_dbo_backupset_backup_finish_date) < DATEADD(hh, -24,GETDATE()))
UNION 

--Databases without any backup history
SELECT  CONVERT(CHAR(100),SERVERPROPERTY('Servername')) AS Server
    , master_dbo_sysdatabases.NAME AS database_name
    , NULL AS [Last Data Backup Date]
    , 9999 AS [Backup Age (Hours)]
FROM master_dbo_sysdatabases LEFT
    JOIN msdb_dbo_backupset
        ON master_dbo_sysdatabases.name = msdb_dbo_backupset_database_name
WHERE msdb_dbo_backupset_database_name IS NULL
AND master_dbo_sysdatabases.name <> 'tempdb' ORDER BY msdb_dbo_backupset_database_name