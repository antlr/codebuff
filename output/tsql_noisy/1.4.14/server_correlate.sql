-- shows all listeners by server, name, environment and component
-- useful for seeing if they are all tied back to a component name
select distinct s.server_name
    , pc.name
    , e.environment_name
    , pc.component_id
from dbo.t_server s
     INNER JOIN
    dbo.t_environment e
        ON s.environment_id = e.environment_id
     INNER JOIN
dbo.t_proc_controller_assoc pca
    ON pca.server_id = s.server_id
     INNER JOIN
dbo.t_proc_controller pc
    ON pc.proc_controller_id = pca.proc_controller_id
order by s.server_name, name, environment_name

select s.server_name, d.dsn_name, rs.server_name AS remote_server, d.database_name
from dbo.t_dsn d
    INNER JOIN
    dbo.t_server s
    ON d.server_id = s.server_id
    INNER JOIN
dbo.t_server rs
    ON d.remote_server_id = rs.server_id
where database_name = '" & db_name & "'
order by 1,3,2

select s.server_name from dbo.t_server s
    INNER JOIN
    dbo.t_dsn d
    ON d.server_id = s.server_id
where s.server_name NOT in ('opsdb.dexma.com', 'opsdb.demo.dexma.com', 'impopsdb.dexma.com', '(local)', 'OPSFH.DEXMA.COM')
union

select s.server_name
from dbo.t_server s
INNER JOIN
     dbo.t_dsn rs
ON rs.remote_server_id = s.server_id
where  s.server_name NOT in ('opsdb.dexma.com', 'opsdb.demo.dexma.com', 'impopsdb.dexma.com', '(local)', 'OPSFH.DEXMA.COM')
       AND active = '1'
order by server_name

select server_name from t_server
where server_id IN (select server_id from t_sched_task )
order by server_name

SELECT s.server_id, s.server_name, st.account, st.app_name, st.working_dir, st.comment, st.creator, st.trigger_string
from dbo.t_server s
    INNER JOIN
    dbo.t_sched_task st
    ON st.server_id = s.server_id
where s.server_name = 'alya'
order by s.server_name

SELECT s.server_name
    , e.environment_name
    , ws.*
from dbo.t_websites ws
    INNER JOIN
    dbo.t_server s
    ON ws.server_id = s.server_id
    INNER JOIN
dbo.t_environment e
    ON s.environment_id = e.environment_id
order by s.server_name