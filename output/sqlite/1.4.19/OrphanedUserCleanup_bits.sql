-- dbamaint
SELECT *
FROM dbusers
WHERE lastupdate IS NOT NULL
      AND ServerLogin LIKE '%** Orphaned **%'
      AND DatabaseUserID NOT IN ('guest', 'INFORMATION_SCHEMA', 'sys', 'cdc', 'BUILTIN\Administrators')
ORDER BY 1, 2, 3

-- Status

SELECT *
FROM SQLDBUsers
WHERE ServerLogin = '** Orphaned **'
      AND DatabaseUserID NOT IN ('guest', 'INFORMATION_SCHEMA', 'sys', 'cdc', 'BUILTIN\Administrators')
      AND ServerName NOT IN ('PSQLRPT21')
ORDER BY 1, 3, 4