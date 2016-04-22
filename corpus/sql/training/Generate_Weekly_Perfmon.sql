insert into CountersDataDaily
select server_id, CountersID, MinValue, MeanAvgValue, MaxValue, Day, GetDate() AS LastUpdate
FROM (
SELECT
        s.server_id, c.CountersID, min(cd.CounterValue) AS MinValue, (sum(cd.CounterValue)-min(cd.CounterValue)-max(cd.CounterValue)) / cast(count(*)-2 as float) AS MeanAvgValue, max(cd.CounterValue) AS MaxValue, convert(char(10), CounterDateTime, 101) AS Day
FROM  
        Counters c INNER JOIN
        CountersData cd ON c.CountersID = cd.CountersID INNER JOIN
        newton.Statusdbot_server s ON s.server_id = cd.ServerID
where  convert(char(10), CounterDateTime, 101) > GetDate() - 3
--where  convert(char(10), CounterDateTime, 101) = (GetDate() - 6)
        GROUP BY  s.server_id, c.CountersID, convert(char(10), CounterDateTime, 101)
) AS f

select server_id, CountersID, MinValue, MeanAvgValue, MaxValue, Day, GetDate() AS LastUpdate
FROM (
SELECT
        s.server_id, c.CountersID, min(cdd.MeanAvgValue) AS MinValue, (sum(cdd.MeanAvgValue)-min(cdd.MeanAvgValue)-max(cdd.MeanAvgValue)) / cast(count(*)-2 as float) AS MeanAvgValue, max(cdd.MaxValue) AS MaxValue, convert(char(10), Date, 101) AS Day
FROM
        Counters c INNER JOIN
        CountersDataDaily cdd ON c.CountersID = cdd.CountersID INNER JOIN
        newton.Statusdbot_server s ON s.server_id = cdd.ServerID
where  convert(char(10), Date, 101) > GetDate() - 3
        GROUP BY  s.server_id, c.CountersID, convert(char(10), Date, 101)
) AS f
order by server_id, ContersID, Day

select datepart(dw,GetDate())
