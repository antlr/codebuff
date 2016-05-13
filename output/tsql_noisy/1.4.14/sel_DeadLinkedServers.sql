SELECT CONVERT(nvarchar(14), sls.SourceServer) AS SourceServer
    , CONVERT(nvarchar(16), sls.DestinationServer) AS DestinationServer
    , sls.LastUpdate
FROM   t_server s
    JOIN SQLLinkedServers sls ON s.server_name = sls.DestinationServer
WHERE s.active = '0'
order by 1,2


SELECT ServerName
    , DBName
    , DatabaseUserID,
       ServerLogin
    , DatabaseRole
FROM   SQLDBUsers
WHERE ServerLogin like '%Orphaned%'
      AND DataBaseUserID NOT IN ('cdc', 'guest', 'INFORMATION_SCHEMA', 'sys')
      AND ServerName NOT LIKE 'PSQLRPT21'
ORDER BY 1,3,4