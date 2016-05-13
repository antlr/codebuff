SELECT *
FROM DriveInfo;
--script to retrieve the values in MB from PS Script output


SELECT
    RTRIM(LTRIM(SUBSTRING(line, 1, CHARINDEX('|', line) - 1))) AS drivename
    , ROUND(CAST(RTRIM(LTRIM(SUBSTRING(line, CHARINDEX('|', line) + 1, ( CHARINDEX('%', line) - 1) - CHARINDEX('|', line)))) AS FLOAT), 0) AS 'capacity(MB)'
    , ROUND(CAST(RTRIM(LTRIM(SUBSTRING(line, CHARINDEX('%', line) + 1, ( CHARINDEX('*', line) - 1) - CHARINDEX('%', line)))) AS FLOAT), 0) AS 'freespace(MB)'
    , ROUND(CAST(RTRIM(LTRIM(SUBSTRING(line, CHARINDEX('|', line) + 1, ( CHARINDEX('%', line) - 1) - CHARINDEX('|', line)))) AS FLOAT) / 1024, 0) AS 'capacity(GB)'
    , ROUND(CAST(RTRIM(LTRIM(SUBSTRING(line, CHARINDEX('%', line) + 1, ( CHARINDEX('*', line) - 1) - CHARINDEX('%', line)))) AS FLOAT) / 1024, 0) AS 'freespace(GB)'
FROM DriveInfo
WHERE line LIKE '[A-Z][:]%'
ORDER BY drivename