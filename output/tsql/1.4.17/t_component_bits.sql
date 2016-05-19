SELECT *
FROM t_component
ORDER BY component_name

SELECT DISTINCT name
FROM t_proc_controller
ORDER BY name

SELECT *
FROM t_component_server_assoc

SELECT DISTINCT
    c.component_name
    , pc.name
FROM dbo.t_component c
    INNER JOIN
    dbo.t_proc_controller pc
        ON c.component_name LIKE pc.name


--

SELECT DISTINCT
    s.server_id
    , s.server_name
    , pca.proc_controller_id
    , pc.name --, csa.server_id, c.component_name, c.component_id
FROM dbo.t_server s
    INNER JOIN
    dbo.t_proc_controller_assoc pca
        ON pca.server_id = s.server_id
    INNER JOIN
    dbo.t_proc_controller pc
        ON pc.proc_controller_id = pca.proc_controller_id
ORDER BY s.server_name


-- dbo.DPC_View

SELECT
    s.server_id
    , s.server_name
    , pc.name
    , pca.cmdLine
    , pca.desktop
    , pca.userName
    , pca.LastUpdate
FROM dbo.t_server s
    INNER JOIN
    dbo.t_proc_controller_assoc pca
        ON pca.server_id = s.server_id
    INNER JOIN
    dbo.t_proc_controller pc
        ON pc.proc_controller_id = pca.proc_controller_id
ORDER BY pca.LastUpdate

-- shows all listeners by server, name, environment and component
-- useful for seeing if they are all tied back to a component name

SELECT DISTINCT
    s.server_name
    , pc.name
    , e.environment_name
    , pc.component_id
FROM dbo.t_server s
    INNER JOIN
    dbo.t_environment e
        ON s.environment_id = e.environment_id
    INNER JOIN
    dbo.t_proc_controller_assoc pca
        ON pca.server_id = s.server_id
    INNER JOIN
    dbo.t_proc_controller pc
        ON pc.proc_controller_id = pca.proc_controller_id
ORDER BY component_id, name, environment_name

SELECT *
FROM t_proc_controller
WHERE component_id IS NULL

SELECT *
FROM t_proc_controller
WHERE name LIKE 'gebilling%'

SELECT *
FROM t_component
WHERE component_name LIKE '%gebilling%'

UPDATE t_proc_controller
SET component_id = '47'
WHERE name LIKE 'gebilling%'

-- shows the associations betweeen a component, queue and server

SELECT DISTINCT
    s.server_name
    , s.server_id
    , c.component_name
    , c.component_id
    , q.queue_name
    , q.queue_id
    , cqa.assoc_id
FROM dbo.t_server s
    INNER JOIN
    dbo.t_component_queue_assoc cqa
        ON cqa.server_id = s.server_id
    INNER JOIN
    dbo.t_component c
        ON c.component_id = cqa.component_id
    INNER JOIN
    dbo.t_queue q
        ON q.queue_id = cqa.queue_id


-- shows all queue->server relations from t_queue_server_assoc

SELECT DISTINCT
    s.server_name
    , s.server_id
    , q.queue_name
    , q.queue_id
    , qsa.LastUpdate
FROM dbo.t_server s
    INNER JOIN
    dbo.t_queue_server_assoc qsa
        ON qsa.server_id = s.server_id
    INNER JOIN
    dbo.t_queue q
        ON q.queue_id = qsa.queue_id
ORDER BY server_name

DELETE FROM t_component_queue_assoc
WHERE server_id = (
                      SELECT server_id
                      FROM t_server
                      WHERE server_name = 'pyxis')

-- shows the queues in the t_queue_server_assoc table tied to the queue_name in t_queue

SELECT DISTINCT
    q.queue_name
    , qsa.queue_id
    , s.server_name
    , qsa.LastUpdate
FROM dbo.t_queue q
    INNER JOIN
    dbo.t_queue_server_assoc qsa
        ON q.queue_id = qsa.queue_id
    INNER JOIN
    dbo.t_server s
        ON s.server_id = qsa.server_id
ORDER BY queue_name

-- shows relationship between component_name and queue_name

SELECT DISTINCT
    c.component_name
    , q.queue_name
FROM dbo.t_component c
    LEFT OUTER JOIN
    dbo.t_queue q
        ON c.component_name = q.queue_name

-- shows all servers and their types including those not classified by type

SELECT
    s.server_id
    , s.server_name
    , st.type_name
    , st.type_id
    , e.environment_name
FROM dbo.t_server s
    LEFT OUTER JOIN
    dbo.t_server_type_assoc sta
        ON sta.server_id = s.server_id
    LEFT OUTER JOIN
    dbo.t_server_type st
        ON st.type_id = sta.type_id
    LEFT OUTER JOIN
    dbo.t_environment e
        ON e.environment_id = s.environment_id
WHERE st.type_id IS NULL AND s.active = '1'
--order by st.type_id
ORDER BY s.server_name

SELECT *
FROM t_queue_server_assoc

UPDATE t_component
SET timestamp = GetDate()
WHERE timestamp IS NULL

UPDATE t_monitoring
SET timestamp = GetDate()
WHERE timestamp IS NULL

SELECT *
FROM t_component
WHERE component_name LIKE 'DirectoryListener%'
ORDER BY component_name

--select * from t_proc_controller where component_id = ' ' order by name

SELECT *
FROM t_proc_controller
WHERE name LIKE 'DirectoryListener_USBankOmni'
ORDER BY name

UPDATE t_proc_controller
SET component_id = '237'
WHERE name LIKE 'DexRealECPreProcessor2%'


DELETE FROM t_proc_controller_assoc
WHERE server_id = (
                      SELECT server_id
                      FROM t_server
                      WHERE server_name = 'boston')

SELECT *
FROM t_proc_controller
ORDER BY name

SELECT *
FROM t_proc_controller
WHERE component_id IS NULL
ORDER BY name

SELECT *
FROM t_component
ORDER BY component_name

SELECT proc_controller_id
FROM t_proc_controller
WHERE name = 'DexFTPAgent2'

UPDATE t_component
SET component_name = 'DexRealECProcessResponse'
WHERE component_name = 'DexRealECProcessoResponse'

UPDATE t_client
SET timestamp = GetDate()

SELECT *
FROM t_client
ORDER BY client_name

SELECT *
FROM t_server
WHERE server_name LIKE 'xqa%'

UPDATE t_server
SET description = ''
WHERE server_name = 'xvm002'

-- insert server id's into the t_monitoring table
INSERT INTO t_monitoring ( server_id ) SELECT DISTINCT s.server_id
                                       FROM dbo.t_server s
                                           LEFT OUTER JOIN
                                           dbo.t_server_type_assoc sta
                                               ON sta.server_id = s.server_id
                                           LEFT OUTER JOIN
                                           dbo.t_server_type st
                                               ON st.type_id = sta.type_id
                                       WHERE s.active = '1'
                                             AND st.type_id NOT LIKE '18'
                                             AND s.server_name NOT LIKE '%dexma.com'