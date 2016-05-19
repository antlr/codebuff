-- remove duplicates from SQLErrorLogs table
SELECT *
FROM SQLErrorLogs a
    JOIN (
             SELECT
                 ServerName
                 , Date
                 , spid
                 , Message
                 , max(seq_num) AS max_seq_num
             FROM SQLErrorLogs
             GROUP BY ServerName, Date, spid, Message
             HAVING count(*) > 1
         ) b ON a.ServerName = b.ServerName AND
                a.Date = b.Date AND
                a.spid = b.spid AND
                a.Message = b.Message AND
                a.seq_num < b.max_seq_num

-----------------------------------------

SELECT *
FROM SQLAgentErrorLogs a
    JOIN (
             SELECT
                 ServerName
                 , Date
                 , ErrorLevel
                 , Message
                 , max(seq_num) AS max_seq_num
             FROM SQLAgentErrorLogs
             GROUP BY ServerName, Date, ErrorLevel, Message
             HAVING count(*) > 1
         ) b ON a.ServerName = b.ServerName AND
                a.Date = b.Date AND
                a.ErrorLevel = b.ErrorLevel AND
                a.Message = b.Message AND
                a.seq_num < b.max_seq_num

SELECT *
FROM tempdb.dboSQLAgentErrorLogsDestination

SELECT *
FROM Status.dboSQLAgentErrorLogs

SELECT *
FROM dbamaint.dbosqlagenterrorlog

SELECT *
FROM ssiserrors
ORDER BY LastUpdate
    DESC