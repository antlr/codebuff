-- dbamaint
select * from dbusers 
where lastupdate is not null  
and ServerLogin like '%** Orphaned **%'
AND DatabaseUserID NOT IN ('guest','INFORMATION_SCHEMA','sys','cdc','BUILTIN\Administrators')  
order by 1,2,3

-- Status
select * from SQLDBUsers
WHERE ServerLogin = '** Orphaned **'
AND DatabaseUserID NOT IN 
	('guest'
	, 'INFORMATION_SCHEMA'
	, 'sys'
	, 'cdc'
	, 'BUILTIN\Administrators')
AND ServerName NOT IN 
	('PSQLRPT21'  -- can't remove users from db's in restore mode
	)
ORDER BY 1,3,4

