/*
This script generates filegroup listing on a given database for each table within the database.
*/

SELECT
    'Object Name' = CASE si.IndID
                    WHEN 1
                        THEN so.Name
                        ELSE si.Name
                    END
    , 'Object Type' = CASE WHEN si.IndID < 2
                          THEN 'Table'
                      ELSE 'Index'
                      END
    , 'Table Name' = CASE WHEN si.IndID > 1
    THEN so.Name
                     ELSE ' '
                     END
    , 'FileGroup Name' = sfg.GroupName
    , 'System FileName' = sf.Name
FROM sysfilegroups sfg
    INNER JOIN sysfiles sf
        ON sfg.groupid = sf.groupid
    INNER JOIN sysindexes si
        ON sfg.groupid = si.groupid
    INNER JOIN sysobjects so
        ON si.id = so.id
WHERE so.type = 'U'
      AND si.Name NOT LIKE '#_%'ESCAPE '#'
      AND so.Name NOT IN ('dtproperties')
ORDER BY 2
    DESC, 3, 1