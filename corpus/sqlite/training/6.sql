SELECT b-c,
       a+b*2+c*3,
       a+b*2,
       c-d,
       (a+b+c+d+e)/5,
       a-b
  FROM t1
 WHERE d NOT BETWEEN 110 AND 150
   AND e+d BETWEEN a+b-10 AND c+130
   AND d>e
