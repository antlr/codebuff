------------------------------------------------------------------------------------------------------------------------
-- all databases compared against DMart_Template_*

SELECT @EmployeeList = COALESCE(@EmployeeList + ', ', '') + CAST(Emp_UniqueID AS VARCHAR(5))
FROM SalesCallsEmployees
WHERE SalCal_UniqueID = 1


SELECT @dblistData = COALESCE(@dblistData + ', ', '') + name
FROM sys.databases
WHERE name LIKE 'DMart%Data'
      AND name NOT LIKE '%Template%'
GROUP BY name


SELECT @dblistStage = COALESCE(@dblistStage + ', ', '') + name
FROM sys.databases
WHERE name LIKE 'DMart%Stage'
      AND name NOT LIKE '%Template%'
GROUP BY name

SELECT @cmd2 = 'E:\Dexma\powershell_bits\Compare-DMartSchema2.ps1 -SqlServerOne ' + @SERVERNAME +
               ' -FirstDatabase DMart_Template_Data -SqlServerTwo ' + @SERVERNAME + ' -DatabaseList ' + @dblistData + ' -Column -Log'

SELECT @cmd3 = 'E:\Dexma\powershell_bits\Compare-DMartSchema2.ps1 -SqlServerOne ' + @SERVERNAME +
               ' -FirstDatabase DMart_Template_Stage -SqlServerTwo ' + @SERVERNAME + ' -DatabaseList ' + @dblistStage + ' -Column -Log'

------------------------------------------------------------------------------------------------------------------------
-- CDC package


SELECT @CDCData = COALESCE(@CDCData + ', ', '') + name
FROM sys.databases
WHERE name LIKE 'DMart%CDC%Data'
      AND name NOT LIKE '%Template%'
GROUP BY name

SELECT @CDCcmd = 'E:\Dexma\powershell_bits\Compare-DMartSchema2.ps1 -SqlServerOne ' + @SERVERNAME +
                 ' -FirstDatabase DMart_TemplateCDC_Data -SqlServerTwo ' + @SERVERNAME + ' -DatabaseList ' + @CDCData + ' -Column -Log'

------------------------------------------------------------------------------------------------------------------------
-- XSQLUTIL18.dbo.Status


SELECT @ServerList = COALESCE(@ServerList + ', ', '') + RTRIM(LTRIM([server_name]))
FROM Status_dbo.t_server s
    INNER JOIN [t_server_type_assoc] sta
        ON s.server_id = sta.server_id
               INNER JOIN [t_server_type] st
        ON sta.type_id = sT.type_id
               INNER JOIN [t_environment] e
        ON s.environment_id = e.environment_id
               INNER JOIN [t_monitoring] m
        ON s.server_id = m.server_id
WHERE type_name = 'DB'
      AND active = 1
GROUP BY server_name
ORDER BY server_name

--print @ServerList

SELECT @cmd = 'E:\Dexma\powershell_bits\Compare-DbamaintSchema.ps1 ' + '-ServerList ' +
              @ServerList +
              ' -Column -Log'

------------------------------------------------------------------------------------------------------------------------

SELECT DISTINCT RTRIM(LTRIM([server_name])) AS ServerName
FROM [t_server] s
    INNER JOIN [t_server_type_assoc] sta
        ON s.server_id = sta.server_id
               INNER JOIN [t_server_type] st
        ON sta.type_id = sT.type_id
               INNER JOIN [t_environment] e
        ON s.environment_id = e.environment_id
               INNER JOIN [t_monitoring] m
        ON s.server_id = m.server_id
WHERE type_name = 'DB'
      AND active = 1
ORDER BY 1

------------------------------------------------------------------------------------------------------------------------
-- PSQLSMC30


SELECT @dblistData = COALESCE(@dblistData + ', ', '') + name
FROM sys.databases
WHERE name LIKE '%SMC'
      AND name NOT LIKE '%Test%'
GROUP BY name


SELECT @dblistStage = COALESCE(@dblistStage + ', ', '') + name
FROM sys.databases
WHERE name LIKE '%SMC'
      AND name NOT LIKE '%Test%'
GROUP BY name

SELECT @cmd2 = 'E:\Dexma\powershell_bits\Compare-DMartSchema2.ps1 -SqlServerOne ' + @SERVERNAME +
               ' -FirstDatabase RLCSMC -SqlServerTwo ' +
               @SERVERNAME + ' -DatabaseList ' + @dblistData + ' -Column -Log'

-- use the Dev current version

SELECT @cmd3 = 'E:\Dexma\powershell_bits\Compare-DMartSchema2.ps1 -SqlServerOne '
               +
               'ISQLDEV610 -FirstDatabase SMCCurrent -SqlServerTwo ' +
               @SERVERNAME + ' -DatabaseList ' + @dblistStage + ' -Column -Log'