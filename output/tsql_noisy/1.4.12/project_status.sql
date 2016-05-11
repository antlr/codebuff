SELECT [PDID], [PSID], [TechID], [PDComment], [HoursWorked], [MinutesWorked], [PDLastUpdate]
    FROM [Status].[ProjectDetail]
where TechID = '4'
order by pdlastupdate desc

update [Status].[ProjectDetail]
    set pdlastupdate = '2011-11-11 11:08:00'
where pdlastupdate = '2011-11-14 11:26:00'

--5849  2533    4   Migrated to Prod.   30  0   2010-11-15 20:43:00

update [Status].[ProjectDetail]
    SET HoursWorked = '3'
        , MinutesWorked = '0'
WHERE PDID = '14854'
          AND PSID = '1697'

SELECT [PSID],
       [TechID],
       [PID],
       [PSTitle],
       [PSTicketNumber],
       [PSTicketCat],
       [PSPriority],
       [PSStatusID],
       [PSActive],
       [PSTargetDate],
       [PSLastUpdate]
FROM [Status].[ProjectStatus]
order by pslastupdate desc


SELECT [PDID], pd.[PSID], ps.PSTitle, pd.[TechID], [PDComment], [HoursWorked], [MinutesWorked], [PDLastUpdate]
FROM [Status].[ProjectDetail] pd
INNER JOIN
    [Status].[ProjectStatus] ps
    ON pd.PSID = ps.PSID
where pd.TechID = '4'
    AND DATEPART(MONTH,PDLastUpdate) IN ( '1', '2', '3' )
    AND DATEPART(YEAR,PDLastUpdate) = '2013'
order by pdlastupdate asc