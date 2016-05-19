-- shows all listeners by server, name, environment and component
-- useful for seeing if they are all tied back to a component name
SELECT DISTINCT
    s.server_name
    , pc.name
    , e.environment_name
    , pc.component_id
FROM dbo.t_server s
    INNER JOIN dbo.t_environment e
        ON s.environment_id = e.environment_id
               INNER JOIN dbo.t_proc_controller_assoc pca
        ON pca.server_id = s.server_id
               INNER JOIN dbo.t_proc_controller pc
        ON pc.proc_controller_id = pca.proc_controller_id
ORDER BY s.server_name, name, environment_name


SELECT
    s.server_name
    , d.dsn_name
    , rs.server_name AS remote_server
    , d.database_name
FROM dbo.t_dsn d
    INNER JOIN dbo.t_server s
        ON d.server_id = s.server_id
               INNER JOIN dbo.t_server rs
        ON d.remote_server_id = rs.server_id
WHERE database_name = '" & db_name & "'
ORDER BY 1, 3, 2


SELECT s.server_name
FROM dbo.t_server s
    INNER JOIN dbo.t_dsn d
        ON d.server_id = s.server_id
WHERE s.server_name NOT IN ('opsdb.dexma.com', 'opsdb.demo.dexma.com', 'impopsdb.dexma.com', '(local)', 'OPSFH.DEXMA.COM')

UNION

SELECT s.server_name
FROM dbo.t_server s
    INNER JOIN dbo.t_dsn rs
        ON rs.remote_server_id = s.server_id
WHERE s.server_name NOT IN ('opsdb.dexma.com', 'opsdb.demo.dexma.com', 'impopsdb.dexma.com', '(local)', 'OPSFH.DEXMA.COM')
      AND active = '1'
ORDER BY server_name

SELECT server_name
FROM t_server
WHERE server_id IN (
                       SELECT server_id
                       FROM t_sched_task)
ORDER BY server_name


SELECT
    s.server_id
    , s.server_name
    , st.account
    , st.app_name
    , st.working_dir
    , st.comment
    , st.creator
    , st.trigger_string
FROM dbo.t_server s
    INNER JOIN dbo.t_sched_task st
        ON st.server_id = s.server_id
WHERE s.server_name = 'alya'
ORDER BY s.server_name


SELECT
    s.server_name
    , e.environment_name
    , ws.*
FROM dbo.t_websites ws
    INNER JOIN dbo.t_server s
        ON ws.server_id = s.server_id
    INNER JOIN dbo.t_environment e
        ON s.environment_id = e.environment_id
ORDER BY s.server_name