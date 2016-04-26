SELECT @ColList = COALESCE(@ColList + CHAR(10) + ', ', '') + c.name
FROM sys.tables t
    JOIN sys.columns c ON c.object_id = t.object_id
WHERE t.name = 'user_profiles'
