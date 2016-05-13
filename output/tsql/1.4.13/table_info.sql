/* Get tables */
SELECT tab.name table_name
FROM sysobjects tab
WHERE tab.xtype = 'U'
      AND tab.name <> 'dtproperties'
ORDER BY 1

/* Get columns */

SELECT
    tab.name table_name
    , col.name column_name
    , col.colid column_id
    , typ.name data_type
    , col.length length
    , col.prec prec
    , col.scale scale
    , com.text default_value
    , obj.name default_cons_name
    , CASE WHEN col.isnullable = 1
          THEN 'Y'
      ELSE 'N'
      END is_nullable
    , CASE WHEN col.status & 80 = 80
          THEN 'Y'
      ELSE 'N'
      END is_identity
FROM sysobjects tab,
    syscolumns col
    LEFT OUTER JOIN sysobjects obj ON com.id = obj.id,
    systypes typ
WHERE tab.id = col.id AND
      tab.xtype = 'U'
      AND tab.name <> 'dtproperties'
      AND col.xusertype = typ.xusertype
ORDER BY 1, 3

/* Get check constraints */

SELECT
    tab.name table_name
    , obj.name constraint_name
    , com.text condition
FROM sysobjects tab,
    syscomments com,
    sysobjects obj
WHERE obj.xtype = 'C'
      AND com.id = obj.id AND
      tab.id = obj.parent_obj AND
      tab.name <> 'dtproperties'
ORDER BY 1, 2

/* Get primary key constraints */

SELECT
    tab.name table_name
    , ind.name constraint_name
    , INDEX_COL(tab.name, ind.indid, idk.keyno) column_name
    , idk.keyno pos
FROM sysobjects tab,
    sysindexes ind,
    sysindexkeys idk
WHERE ind.status & 800 = 800
      AND ind.id = tab.id AND
      idk.id = tab.id AND
      idk.indid = ind.indid AND
      tab.name <> 'dtproperties'
ORDER BY 1, 2, 4

/* Get unique key constraints */

SELECT
    tab.name table_name
    , ind.name constraint_name
    , INDEX_COL(tab.name, ind.indid, idk.keyno) column_name
    , idk.keyno pos
FROM sysobjects tab,
    sysindexes ind,
    sysindexkeys idk
WHERE ind.status & 1000 = 1000
      AND ind.id = tab.id AND
      idk.id = tab.id AND
      idk.indid = ind.indid AND
      tab.name <> 'dtproperties'
ORDER BY 1, 2, 4

/* Get foreign key constraints */

SELECT
    child_table          child_table
    , obj.name constraint_name
    , child_column child_column
    , child_pos pos
    , parent_table parent_table
    , parent_column parent_column
FROM (
         SELECT
             tab1.name child_table
             , col1.name child_column
             , CASE col1.colid
               WHEN ref.fkey1
                   THEN 1
               WHEN ref.fkey2
                   THEN 2
               WHEN ref.fkey3
                   THEN 3
               WHEN ref.fkey4
                   THEN 4
               WHEN ref.fkey5
                   THEN 5
               WHEN ref.fkey6
                   THEN 6
               WHEN ref.fkey7
                   THEN 7
               WHEN ref.fkey8
                   THEN 8
               WHEN ref.fkey9
                   THEN 9
               WHEN ref.fkey10
                   THEN 10
               WHEN ref.fkey11
                   THEN 11
               WHEN ref.fkey12
                   THEN 12
               WHEN ref.fkey13
                   THEN 13
               WHEN ref.fkey14
                   THEN 14
               WHEN ref.fkey15
                   THEN 15
               WHEN ref.fkey16
                   THEN 16 END child_pos
             , tab2.name parent_table
             , col2.name parent_column
             , ref.constid constraint_id
             , CASE col2.colid
               WHEN ref.rkey1
                   THEN 1
               WHEN ref.rkey2
                   THEN 2
               WHEN ref.rkey3
                   THEN 3
               WHEN ref.rkey4
                   THEN 4
               WHEN ref.rkey5
                   THEN 5
               WHEN ref.rkey6
                   THEN 6
               WHEN ref.rkey7
                   THEN 7
               WHEN ref.rkey8
                   THEN 8
               WHEN ref.rkey9
                   THEN 9
               WHEN ref.rkey10
                   THEN 10
               WHEN ref.rkey11
                   THEN 11
               WHEN ref.rkey12
                   THEN 12
               WHEN ref.rkey13
                   THEN 13
               WHEN ref.rkey14
                   THEN 14
               WHEN ref.rkey15
                   THEN 15
               WHEN ref.rkey16
                   THEN 16 END parent_pos
         FROM syscolumns col1,
             sysobjects tab1,
             syscolumns col2,
             sysobjects tab2,
             sysreferences ref
         WHERE col1.id = ref.fkeyid AND
tab1.id = col1.id AND
col2.id = ref.rkeyid AND
tab2.id = col2.id AND
col1.colid IN
(ref.fkey1, ref.fkey2
          , ref.fkey3
          , ref.fkey4
          , ref.fkey5
          , ref.fkey6
          , ref.fkey7
          , ref.fkey8
          , ref.fkey9
          , ref.fkey10
          , ref.fkey11
          , ref.fkey12
          , ref.fkey13
          , ref.fkey14
          , ref.fkey15
          , ref.fkey16)
                                    AND col2.colid IN
                                        (ref.rkey1, ref.rkey2
                                                  , ref.rkey3
                                                  , ref.rkey4
                                                  , ref.rkey5
                                                  , ref.rkey6
                                                  , ref.rkey7
                                                  , ref.rkey8
                                                  , ref.rkey9
                                                  , ref.rkey10
                                                  , ref.rkey11
                                                  , ref.rkey12
                                                  , ref.rkey13
                                                  , ref.rkey14
                                                  , ref.rkey15
                                                  , ref.rkey16)
                                    AND tab1.name <> 'dtproperties'
     ) foreignkeycols,
    sysobjects obj
WHERE child_pos = parent_pos AND obj.id = constraint_id
ORDER BY 1, 2, 4

/* Get indexes except primary keys and keys enforcing unique constraints */

SELECT
    tab.name table_name
    , ind.name index_name
    , INDEX_COL(tab.name, ind.indid, idk.keyno) column_name
    , CASE WHEN ind.status & 2 = 2
          THEN 'Y'
      ELSE 'N'
      END is_unique
FROM sysindexes ind,
    sysindexkeys idk,
    sysobjects tab
WHERE NOT(ind.status & 800 = 800)
      AND NOT(ind.status & 1000 = 1000)
      AND idk.id = tab.id AND
      idk.indid = ind.indid AND
      tab.xtype = 'U'
      AND tab.id = ind.id AND
      tab.name <> 'dtproperties'
ORDER BY 1, 2