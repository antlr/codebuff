select * from document_orders

SELECT do_id
    , ln_loan_id,
       do_document_category,
       do_date_initiated
    , do_date_received
    , do_last_update
    , DATEDIFF(day, do_date_initiated, do_date_received) AS DaysBetween
from document_orders
where  do_document_category LIKE '%REALEC%'
       AND ( do_date_initiated IS NULL
             OR    do_date_received IS NULL
             OR    do_last_update IS NULL )
ORDER BY DATEDIFF(day, do_date_initiated, do_date_received) desc



SELECT 'ZeroToThirty' = COUNT ( CASE WHEN DATEDIFF(day, do_date_initiated, do_date_received) BETWEEN 0 AND 29 THEN 'DaysBetween' END ),
       COUNT ( CASE WHEN DATEDIFF(day, do_date_initiated, do_date_received) BETWEEN 30 AND 59 THEN 'DaysBetween' END ) AS ThirtyToSixty,
       COUNT ( CASE WHEN DATEDIFF(day, do_date_initiated, do_date_received) BETWEEN 60 AND 89 THEN 'DaysBetween' END ) AS SixtyToNinety
                                                                                                                                      , COUNT ( CASE WHEN DATEDIFF(day, do_date_initiated, do_date_received) >= 90 THEN 'DaysBetween' END ) AS NinetyPlus
from document_orders
where do_document_category LIKE '%REALEC%'