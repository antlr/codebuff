Select * from sys.dm_exec_query_optimizer_info
where counter in ('optimizations','elapsed time','trivial plan','tables','insert stmt','update stmt','delete stmt')


------------------------------------------------------------------------
-- Retrieve Parallel Statements With the Highest Worker Time

SELECT qs.total_worker_time,
			qs.total_elapsed_time,
            SUBSTRING(qt.text,qs.statement_start_offset/2, 
			(case when qs.statement_end_offset = -1 
			then len(convert(nvarchar(max), qt.text)) * 2 
			else qs.statement_end_offset end -qs.statement_start_offset)/2) 
		as query_text,
		qt.dbid, dbname=db_name(qt.dbid),
		qt.objectid,
		qs.sql_handle,
		qs.plan_handle
FROM sys.dm_exec_query_stats qs
where qs.total_worker_time > qs.total_elapsed_time
ORDER BY 
       qs.total_worker_time DESC

------------------------------------------------------------------------
-- Retrieve Statements with the Highest Plan Re-Use Counts

SELECT  qs.sql_handle
		,qs.plan_handle
		,cp.cacheobjtype
		,cp.usecounts
		,cp.size_in_bytes  
		,qs.statement_start_offset
		,qs.statement_end_offset
		,qt.dbid
		,qt.objectid
		,qt.text
		,SUBSTRING(qt.text,qs.statement_start_offset/2, 
			(case when qs.statement_end_offset = -1 
			then len(convert(nvarchar(max), qt.text)) * 2 
			else qs.statement_end_offset end -qs.statement_start_offset)/2) 
		as statement
FROM sys.dm_exec_query_stats qs
inner join sys.dm_exec_cached_plans as cp on qs.plan_handle=cp.plan_handle
where cp.plan_handle=qs.plan_handle
--and qt.dbid = db_id()
ORDER BY [dbid],[Usecounts] DESC

------------------------------------------------------------------------
-- Retrieve Statements with the Lowest Plan Re-Use Counts

SELECT  cp.cacheobjtype
		,cp.usecounts
		,size=cp.size_in_bytes  
		,stmt_start=qs.statement_start_offset
		,stmt_end=qs.statement_end_offset
		,qt.dbid
		,qt.objectid
		,qt.text
		,SUBSTRING(qt.text,qs.statement_start_offset/2, 
			(case when qs.statement_end_offset = -1 
			then len(convert(nvarchar(max), qt.text)) * 2 
			else qs.statement_end_offset end -qs.statement_start_offset)/2) 
		as statement
		,qs.sql_handle
		,qs.plan_handle
FROM sys.dm_exec_query_stats qs
inner join sys.dm_exec_cached_plans as cp on qs.plan_handle=cp.plan_handle
where cp.plan_handle=qs.plan_handle
and qt.dbid is NULL
ORDER BY [usecounts],[statement] asc


------------------------------------------------------------------------
-- List Statements With the Highest Average CPU Time

SELECT  qs.total_worker_time/qs.execution_count as [Avg CPU Time],
        SUBSTRING(qt.text,qs.statement_start_offset/2, 
			(case when qs.statement_end_offset = -1 
			then len(convert(nvarchar(max), qt.text)) * 2 
			else qs.statement_end_offset end -qs.statement_start_offset)/2) 
		as query_text,
		qt.dbid, dbname=db_name(qt.dbid),
		qt.objectid 
FROM sys.dm_exec_query_stats qs
ORDER BY 
        [Avg CPU Time] DESC


------------------------------------------------------------------------
--List Statements with the Highest Execution Counts

SELECT  qs.execution_count,
        SUBSTRING(qt.text,qs.statement_start_offset/2, 
			(case when qs.statement_end_offset = -1 
			then len(convert(nvarchar(max), qt.text)) * 2 
			else qs.statement_end_offset end -qs.statement_start_offset)/2) 
		as query_text,
		qt.dbid, dbname=db_name(qt.dbid),
		qt.objectid 
FROM sys.dm_exec_query_stats qs
ORDER BY 
        qs.execution_count DESC
