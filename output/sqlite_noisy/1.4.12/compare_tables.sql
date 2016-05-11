SELECT MAX(TableName) as TableName, server_id, server_name, environment_id, description, active, LastUpdate
FROM
(
SELECT 'OSQLUTIL12_Status_dbo.t_server' as TableName
    , server_id
    , server_name
    , environment_id
    , description
    , active
    , LastUpdate
FROM OSQLUTIL12_Status_dbo.t_server
UNION ALL


SELECT 'ISQLCBS510_StatusIMP_dbo.t_server' as TableName
   , server_id
   , server_name
   , environment_id
   , description
   , active
   , LastUpdate
FROM ISQLCBS510_StatusIMP_dbo.t_server
) tmp
GROUP BY server_id, server_name, environment_id, description, active, LastUpdate HAVING COUNT(*) = 1
ORDER BY server_id