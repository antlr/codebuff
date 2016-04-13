SELECT c-d,
       b,
       e,
       (SELECT count(*) FROM t1 AS x WHERE x.b<t1.b)
  FROM t1
 WHERE a>b
 ORDER BY 4,3,2,1
