SELECT b-c,
       d-e,
       (SELECT count(*) FROM t1 AS x WHERE x.c>t1.c AND x.d<t1.d),
       a+b*2+c*3,
       (a+b+c+d+e)/5,
       a-b
  FROM t1
 WHERE (e>c OR e<d)
 ORDER BY 6,1,5
