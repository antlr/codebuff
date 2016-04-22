--SELECT * FROM IPMon
SELECT * FROM IPMonAlerts
----------------------------------------
-- Dependancy = 1 are PING monitors
SELECT * FROM IPMonGroupMembers
	ORDER BY 1, 2
----------------------------------------	
SELECT * FROM IPMonGroups
	ORDER BY 4
----------------------------------------
SELECT * FROM IPMonMonitors
	-- MonitorID	--> IPMonMonitors
	-- DeviceID		--> 
	-- TypeID		--> IPMonTypeAssoc
----------------------------------------
SELECT * FROM IPMonMPMembers
	-- MonitorID	--> IPMonMonitors
	-- MainID		--> IPMonAlerts
----------------------------------------
SELECT * FROM IPMonTypeAssoc
	ORDER BY 1
----------------------------------------
SELECT * FROM t_server_type

--SELECT * FROM IPMonProfiles
--SELECT * FROM IPMonProfileMembers
--SELECT * FROM IPMonMaintanenceProfiles

SELECT * FROM IPMonMonitors


SELECT  DeviceID, MonitorID, Name, [Address], imm.TypeID,ita.monitor_category, [Description] --* 
FROM IPMonMonitors imm
	INNER JOIN IPMonTypeAssoc ita ON imm.TypeID = ita.typeid
	--INNER JOIN IPMonGroupMembers igm ON imm.MonitorID = igm.MonitorID
GROUP BY DeviceID, MonitorID, ita.monitor_category, Name, [Address], imm.TypeID, [Description]
------------------------------------------------------------------------------------------------------------------------
-- missing 463 or so rows
except
SELECT  DeviceID, imm.MonitorID, Name, [Address], imm.TypeID, ita.monitor_category, [Description] --* 
FROM IPMonMonitors imm
	INNER JOIN IPMonTypeAssoc ita ON imm.TypeID = ita.typeid
	INNER JOIN IPMonGroupMembers igm ON imm.MonitorID = igm.MonitorID
GROUP BY DeviceID, imm.MonitorID, imm.TypeID, ita.monitor_category, Name, [Address], [Description]
------------------------------------------------------------------------------------------------------------------------
-- this adds a lot of extra rows as it has multiple groups for most monitors
SELECT DeviceID
	, imm.MonitorID
	, igm.GroupID
	, [Address]
	, imm.Name
	, ita.monitor_category
	, imm.TypeID
	, [Description]
FROM IPMonMonitors imm
	INNER JOIN IPMonTypeAssoc ita ON imm.TypeID = ita.typeid
	INNER JOIN IPMonGroupMembers igm ON imm.MonitorID = igm.MonitorID
--WHERE Address = 'PSQLMET31'	
GROUP BY DeviceID
	, imm.MonitorID
	, igm.GroupID
	, ita.monitor_category
	, [Address]
	, [Description]
	, imm.TypeID
	, imm.Name;
------------------------------------------------------------------------------------------------------------------------
-- PIVOT on GroupNames


SELECT @GroupNames = COALESCE(@GroupNames + '], [', '') + GroupName
	FROM IPMonGroups
	GROUP BY GroupName
	ORDER BY GroupName

SELECT @SQL = 
'SELECT *
	, ' + @GroupNames + '
FROM
(
SELECT DeviceID
	, imm.MonitorID AS MonitorID
	, igm.GroupID AS GroupID
	, ig.GroupName AS GroupName
	, [Address]
	, imm.Name AS Name
	, ita.monitor_category AS MonitorCategory
	, imm.TypeID AS TypeID
	, [Description]
FROM IPMonMonitors imm
	INNER JOIN IPMonTypeAssoc ita ON imm.TypeID = ita.typeid
	INNER JOIN IPMonGroupMembers igm ON imm.MonitorID = igm.MonitorID
	INNER JOIN IPMonGroups ig ON igm.GroupID = ig.GroupID
--WHERE Address = ''PSQLMET31''	
GROUP BY DeviceID
	, imm.MonitorID
	, igm.GroupID
	, ig.GroupName
	, ita.monitor_category
	, [Address]
	, [Description]
	, imm.TypeID
	, imm.Name) AS SourceTable
PIVOT
(
COUNT(GroupID)
FOR GroupName IN (
' + @GroupNames + '
)
) AS PivotTable;'	


------------------------------------------------------------------------------------------------------------------------

SELECT * FROM t_server 
WHERE Active = 1
ORDER BY 2


SELECT * FROM t_server_type
ORDER BY 1


SELECT * FROM t_server_type_assoc
ORDER BY 2


SELECT s.server_name, s.description, st.[type_name]
FROM t_server s 
	JOIN t_server_type_assoc sta ON sta.server_id = s.server_id
	JOIN t_server_type st ON sta.type_id = st.type_id
WHERE s.active = 1
ORDER BY 3,1
------------------------------------------------------------------------------------------------------------------------

