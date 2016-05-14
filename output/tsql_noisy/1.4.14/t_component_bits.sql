
select * from t_component
order by component_name

select distinct name
from t_proc_controller
order by name

select * from t_component_server_assoc

SELECT distinct c.component_name
    , pc.name
from dbo.t_component c
     INNER JOIN
    dbo.t_proc_controller pc
        ON c.component_name like pc.name


-- 

select distinct s.server_id
    , s.server_name
    , pca.proc_controller_id,
                pc.name--, csa.server_id, c.component_name, c.component_id
from dbo.t_server s
     INNER JOIN
    dbo.t_proc_controller_assoc pca
        ON pca.server_id = s.server_id
     INNER JOIN
dbo.t_proc_controller pc
    ON pc.proc_controller_id = pca.proc_controller_id
order by s.server_name   


-- dbo.DPC_View

select s.server_id
    , s.server_name
    , pc.name
    , pca.cmdLine
    , pca.desktop
    , pca.userName
    , pca.LastUpdate
from dbo.t_server s
    INNER JOIN
    dbo.t_proc_controller_assoc pca
    ON pca.server_id = s.server_id
    INNER JOIN
dbo.t_proc_controller pc
    ON pc.proc_controller_id = pca.proc_controller_id
ORDER BY pca.LastUpdate

-- shows all listeners by server, name, environment and component
-- useful for seeing if they are all tied back to a component name

select distinct s.server_name
    , pc.name
    , e.environment_name,
                pc.component_id
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
order by component_id, name, environment_name

select * from t_proc_controller
where component_id is NULL

select * from t_proc_controller
where name like 'gebilling%'

select * from t_component
where component_name like '%gebilling%'

update t_proc_controller
set component_id = '47'
where name like 'gebilling%'

-- shows the associations betweeen a component, queue and server

select distinct s.server_name, s.server_id, c.component_name, c.component_id, q.queue_name, q.queue_id, cqa.assoc_id
from dbo.t_server s
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

select distinct s.server_name
    , s.server_id
    , q.queue_name,
                q.queue_id
    , qsa.LastUpdate
from dbo.t_server s
     INNER JOIN
    dbo.t_queue_server_assoc qsa
        ON qsa.server_id = s.server_id
     INNER JOIN
dbo.t_queue q
    ON q.queue_id = qsa.queue_id
order by server_name

delete from t_component_queue_assoc
where server_id = (select server_id from t_server
                   where server_name = 'pyxis')

-- shows the queues in the t_queue_server_assoc table tied to the queue_name in t_queue

select distinct q.queue_name
    , qsa.queue_id
    , s.server_name,
                qsa.LastUpdate
from dbo.t_queue q
     INNER JOIN
    dbo.t_queue_server_assoc qsa
        ON q.queue_id = qsa.queue_id
     INNER JOIN
dbo.t_server s
    ON s.server_id = qsa.server_id
order by queue_name

-- shows relationship between component_name and queue_name

select distinct c.component_name
    , q.queue_name
from dbo.t_component c
     LEFT OUTER JOIN
    dbo.t_queue q
        ON c.component_name = q.queue_name

-- shows all servers and their types including those not classified by type

SELECT s.server_id
    , s.server_name
    , st.type_name,
       st.type_id
    , e.environment_name
from dbo.t_server s
LEFT OUTER JOIN
    dbo.t_server_type_assoc sta
ON sta.server_id = s.server_id
LEFT OUTER JOIN
dbo.t_server_type st
ON st.type_id = sta.type_id
LEFT OUTER JOIN
dbo.t_environment e
ON e.environment_id = s.environment_id
where  st.type_id IS NULL
and s.active = '1'
--order by st.type_id
order by s.server_name

select * from t_queue_server_assoc

update t_component
set timestamp = GetDate()
where timestamp is NULL


update t_monitoring
set timestamp = GetDate()
where timestamp is NULL

select * from t_component
where component_name like 'DirectoryListener%'
order by component_name

--select * from t_proc_controller where component_id = ' ' order by name

select * from t_proc_controller
where name like 'DirectoryListener_USBankOmni'
order by name

update t_proc_controller
set component_id = '237'
where name like 'DexRealECPreProcessor2%'

delete from t_proc_controller_assoc
where server_id = (select server_id from t_server
                   where server_name = 'boston')

select * from t_proc_controller
order by name

select * from t_proc_controller
where component_id is null
order by name

select * from t_component
order by component_name

select proc_controller_id from t_proc_controller
where name = 'DexFTPAgent2'

update t_component
set component_name = 'DexRealECProcessResponse'
where component_name = 'DexRealECProcessoResponse'

update t_client
set timestamp = GetDate()

select * from t_client
order by client_name

select * from t_server
where server_name like 'xqa%'

update t_server
set description = ''
where server_name = 'xvm002'

-- insert server id's into the t_monitoring table

insert into t_monitoring ( server_id ) select distinct s.server_id
                                       from dbo.t_server s
                                            LEFT OUTER JOIN
                                           dbo.t_server_type_assoc sta
                                               ON sta.server_id = s.server_id
                                            LEFT OUTER JOIN
dbo.t_server_type st
    ON st.type_id = sta.type_id
                                       where  s.active = '1'
                                              and st.type_id not like '18'
                                              and s.server_name not like '%dexma.com'