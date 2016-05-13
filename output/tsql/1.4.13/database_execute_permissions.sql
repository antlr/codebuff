-- searches all databases for GRANT'ed permissions
-- ignores SQL accounts and domain users

INSERT INTO dbs SELECT name
                 FROM sys.databases
ORDER BY name

-- can be changed to do windows users easily

SELECT 'USE ' + database_name + char(10) + 'GO' + CHAR(10) + permission + ' ' + action_type + ' ON ' + objectname + ' TO ' + '''' + '''' + CHAR(10) + 'GO' + CHAR(10)
FROM permission
WHERE object_type IS NOT NULL
      AND objectname IS NOT NULL
      AND account_type = 'WINDOWS_GROUP'
ORDER BY database_name, user_role_name