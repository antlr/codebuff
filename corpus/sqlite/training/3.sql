SELECT b-c,
       c-d,
       a+b*2
  FROM t1
 WHERE c>d
    OR d>e
    OR (e>a AND e<b)
 ORDER BY 3,1
