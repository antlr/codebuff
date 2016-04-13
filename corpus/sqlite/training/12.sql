SELECT a+b*2+c*3+d*4,
       (a+b+c+d+e)/5,
       abs(b-c)
  FROM t1
 WHERE (c<=d-2 OR c>=d+2)
 ORDER BY 3,1,2
