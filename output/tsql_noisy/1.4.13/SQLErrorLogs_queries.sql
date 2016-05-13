-- remove duplicates from SQLErrorLogs table
select * from SQLErrorLogs a
    join (
         select ServerName
             , Date
             , spid
             , Message
             , max(seq_num) AS max_seq_num
         from   SQLErrorLogs
         group by ServerName, Date, spid, Message
         having count (*) > 1
         ) b on a.ServerName = b.ServerName
                and a.Date = b.Date
                and a.spid = b.spid
                and a.Message = b.Message
                and a.seq_num < b.max_seq_num

-----------------------------------------

select * from SQLAgentErrorLogs a
    join (
         select ServerName, Date, ErrorLevel, Message, max(seq_num) AS max_seq_num
         from   SQLAgentErrorLogs
         group by ServerName, Date, ErrorLevel, Message
         having count (*) > 1
         ) b on a.ServerName = b.ServerName
                and a.Date = b.Date
                and a.ErrorLevel = b.ErrorLevel
                and a.Message = b.Message
                and a.seq_num < b.max_seq_num

select * from tempdb.dboSQLAgentErrorLogsDestination

select * from Status.dboSQLAgentErrorLogs

select * from dbamaint.dbosqlagenterrorlog

select * from ssiserrors
order by LastUpdate desc