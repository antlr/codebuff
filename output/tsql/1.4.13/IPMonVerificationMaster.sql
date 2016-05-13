SELECT DISTINCT
    t.server_name
    , t.server_id
    , 'PING'             AS missingmonitors
FROM t_server t
    INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
      tsta.type_id IN
      ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
      AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
      AND t.server_name NOT IN
          ('XWEBUTIL12', 'XWEBUTIL13', 'EWEBPROD1', 'PWEBSVC20', 'PWEBUSB20', 'XSQLUTIL11')
      AND t.server_name NOT IN (
    SELECT DISTINCT l.address
    FROM ipmongroups g
        INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                    INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                    INNER JOIN t_server t ON l.address = t.server_name
                    INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
    WHERE l.name LIKE '%PING%'
          AND t.environment_id = 0
          AND tsta.type_id IN
              ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
          AND g.groupname IN ('Prod Ping')
          AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
    t.server_name
    , t.server_id
    , 'Remote Procedure Call'             AS missingmonitors
FROM t_server t
    INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
      tsta.type_id IN
      ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
      AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
      AND t.server_name NOT IN
          ('XWEBUTIL12', 'XWEBUTIL13', 'EWEBPROD1', 'XSQLUTIL11')
      AND t.server_name NOT IN (
          SELECT DISTINCT l.address
          FROM ipmongroups g
              INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                          INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                          INNER JOIN t_server t ON l.address = t.server_name
                          INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
          WHERE l.name LIKE '%Remote Procedure Call%'
                AND t.environment_id = 0
                AND tsta.type_id IN
                    ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
                AND g.groupname IN ('Prod O/S Services')
                AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'Task Scheduler'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN
     ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT IN
         ('XWEBUTIL12', 'XWEBUTIL13', 'XSQLUTIL11', 'EWEBPROD1', 'PVM400')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%Task Scheduler%'
               AND t.environment_id = 0
               AND tsta.type_id IN
                   ('1', '2', '3', '4', '5', '6', '7', '8', '9', '15', '22', '24', '26', '29')
               AND g.groupname IN ('Prod O/S Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'Message Queuing Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN ('8')
     AND t.environment_id = 0
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%Message Queuing Service%'
               AND t.environment_id = 0
               AND tsta.type_id IN ('8')
               AND g.groupname IN ('Prod O/S Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'SQL Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id = 1 --DB Server
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT IN ('XSQLUTIL11')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%SQL Server (MSSQLSERVER)%'
               AND t.environment_id = 0
               AND tsta.type_id = 1 --DB Server
               AND g.groupname IN ('Prod SQL Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'SQL Agent Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id = 1 --DB Server
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT IN
         ('XSQLUTIL11', 'EWEBPROD1')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%SQL Server Agent (MSSQLSERVER)%'
               AND t.environment_id = 0
               AND tsta.type_id = 1 --DB Server
               AND g.groupname IN ('Prod SQL Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'SQL Integration Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id = 1 --DB Server
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT IN ('XSQLUTIL11')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%SQL Server Integration Services%'
               AND t.environment_id = 0
               AND tsta.type_id = 1 --DB Server
               AND g.groupname IN ('Prod SQL Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'IIS Admin Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN
     ('2', '4', '30', '31')
      --Web, Connection, FTP, Internal FTP server
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT LIKE 'PWEBSVC%'
      -- 2008 R2 servers do not have the IIS Admin service
     AND t.server_name NOT IN
         ('PWEBUSB20', 'PWEBUSB21', 'XWEBUTIL12', 'XWEBUTIL13', 'EWEBPROD1', 'PWEBBO20', 'PWEBBO21', 'PWEBMET20', 'PWEBMET21')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
--where l.name like '%IIS Admin Service%'
         WHERE l.name LIKE '%IIS Admin%'
               AND t.environment_id = 0
               AND tsta.type_id IN
                   ('2', '4', '30', '31')
          --Web, Connection, FTP, Internal FTP server
               AND g.groupname IN ('Prod IIS Services')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'W3SVC Service'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN ('2') --Web Server
     AND t.environment_id = 0
      -- exclude servers that don't meet the requirements
     AND t.server_name NOT IN
         ('XWEBUTIL12', 'XWEBUTIL13', 'EWEBPROD1')
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%World Wide Web%'
               AND t.environment_id = 0
               AND tsta.type_id IN ('2') --Web Server
               AND g.groupname IN ('Prod IIS Services')
               AND t.active = 1)EXCEPT
                                SELECT
                                    t.server_name
                                    , t.server_id
                                    , 'W3SVC Service'             AS missingmonitors --exclude connection servers
                                FROM t_server t
                                    INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
                                WHERE tsta.type_id IN ('4')
UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'FTP Port Access'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN
     ('4', '30', '31') --Connection, FTP or Internal FTP
     AND t.environment_id = 0
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%ftp'
               AND t.environment_id = 0
               AND tsta.type_id IN
                   ('4', '30', '31')
          --Connection, FTP or Internal FTP
               AND g.groupname IN ('Prod FTP Port')
               AND t.active = 1)
UNION ALL
---------------------------------------------------------------------------------------------------
--SELECT DISTINCT t.server_name,
--                t.server_id,
--                'SMTP Service' AS missingmonitors
--FROM   t_server t
--       INNER JOIN t_server_type_assoc tsta
--         ON t.server_id = tsta.server_id
--WHERE  t.active = 1
--       AND tsta.type_id IN ( '2' ) --Web Server
--       AND t.environment_id = 0
--       AND t.server_name NOT IN (SELECT DISTINCT l.address
--                                 FROM   ipmongroups g
--                                        INNER JOIN ipmongroupmembers m
--                                          ON g.groupid = m.groupid
--                                        INNER JOIN ipmonmonitors l
--                                          ON m.monitorid = l.monitorid
--                                        INNER JOIN t_server t
--                                          ON l.address = t.server_name
--                                        INNER JOIN t_server_type_assoc tsta
--                                          ON t.server_id = tsta.server_id
--                                 WHERE  l.name LIKE '%Simple Mail Transfer%'
--                                        AND t.environment_id = 0
--                                        AND tsta.type_id IN ( '2' ) --Web Server
--                                        AND g.groupname IN ( 'Prod IIS Services'
--                                                           )
--                                        AND t.active = 1)
--EXCEPT
--(SELECT t.server_name,
--        t.server_id,
--        'SMTP Service' AS missingmonitors --exclude connection and fax servers
-- FROM   t_server t
--        INNER JOIN t_server_type_assoc tsta
--          ON t.server_id = tsta.server_id
-- WHERE  tsta.type_id IN ( '4', '26' ))
--UNION ALL
---------------------------------------------------------------------------------------------------
SELECT DISTINCT
   t.server_name
   , t.server_id
   , 'File Access'             AS missingmonitors
FROM t_server t
   INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
WHERE t.active = 1 AND
     tsta.type_id IN ('5')
     AND t.environment_id = 0
     AND t.server_name NOT LIKE 'pdoc11'
      --This is a quasi standby server as i have been told. as such this monitor doesn't apply and wont work.
     AND t.server_name NOT IN (
         SELECT DISTINCT l.address
         FROM ipmongroups g
             INNER JOIN ipmongroupmembers m ON g.groupid = m.groupid
                         INNER JOIN ipmonmonitors l ON m.monitorid = l.monitorid
                         INNER JOIN t_server t ON l.address = t.server_name
                         INNER JOIN t_server_type_assoc tsta ON t.server_id = tsta.server_id
         WHERE l.name LIKE '%File Access%'
               AND t.environment_id = 0
               AND tsta.type_id IN ('5')
               AND g.groupname IN ('Prod File Access')
               AND t.active = 1)
--UNION ALL
--SELECT DISTINCT url           AS server_name,
--                '0'           AS server_id,
--                'ASP Monitor' AS missingmonitors
----from stgsqlops510.ops.dbo.sites --Stage
--FROM   psqlsvc21.ops.dbo.sites --Prod
--WHERE  url NOT IN (SELECT address
--                   FROM   xsqlutil18.status.dbo.ipmonmonitors
--                   WHERE  typeid = '6')
--ORDER  BY server_name, missingmonitors
ORDER BY missingmonitors, server_name