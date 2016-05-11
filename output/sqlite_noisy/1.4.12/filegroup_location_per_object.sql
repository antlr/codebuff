/*
This script generates filegroup listing on a given database for each table within the database.
*/

select 'Object Name' = case si.IndID when 1 then so.Name else si.Name end,
        'Object Type' = case when si.IndID < 2 then 'Table' else 'Index'
            end,
        'Table Name' = case when si.IndID > 1 then so.Name else ' '
            end,
        'FileGroup Name' = sfg.GroupName,
        'System FileName' = sf.Name
from sysfilegroups sfg
inner join sysfiles sf
on sfg.groupid = sf.groupid
inner join sysindexes si
on sfg.groupid = si.groupid
inner join sysobjects so
on si.id = so.id
where so.type = 'U'
and si.Name not like '#_%'escape '#'
and so.Name not in ('dtproperties' )
order by 2 desc,3,1