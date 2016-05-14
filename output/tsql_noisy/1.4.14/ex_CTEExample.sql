/*
http://www.sqlservercentral.com/articles/T-SQL/62159/
*/

CREATE TABLE [dbo].[Items]
    ( [ItemId][int] NOT NULL, [Item][varchar](100) NOT NULL)
CREATE TABLE [dbo].[PriceHistory]
    ( [ItemId][int] NOT NULL, [PriceStartDate][datetime] NOT NULL, [Price][decimal](10,2) NOT NULL )

SELECT currow.Item,
       prevrow.Price AS OldPrice
                  , currow.Price AS RangePrice
                  , currow.PriceStartDate AS StartDate
                  , nextrow.PriceStartDate AS EndDate
FROM PriceCompare currow
LEFT JOIN PriceCompare nextrow
    ON currow.rownum = nextrow.rownum -1 AND currow.ItemId = nextrow.ItemId
LEFT JOIN PriceCompare prevrow
    ON currow.rownum = prevrow.rownum +1 AND currow.ItemId = prevrow.ItemId
INSERT INTO Items VALUES (1, 'vacuum cleaner')

INSERT INTO Items VALUES (2, 'washing machine')

INSERT INTO Items VALUES (3, 'toothbrush')

INSERT INTO PriceHistory VALUES (1, '2004-03-01',         250)

INSERT INTO PriceHistory VALUES (1, '2005-06-15', 219.99)

INSERT INTO PriceHistory VALUES (1, '2007-01-03', 189.99)

INSERT INTO PriceHistory VALUES (1, '2007-02-03', 200.00)

INSERT INTO PriceHistory VALUES (2, '2006-07-12', 650.00)

INSERT INTO PriceHistory VALUES (2, '2007-01-03', 550.00)

INSERT INTO PriceHistory VALUES (3, '2005-01-01', 1.99)

INSERT INTO PriceHistory VALUES (3, '2006-01-01', 1.79)

INSERT INTO PriceHistory VALUES (3, '2007-01-01', 1.59)

INSERT INTO PriceHistory VALUES (3, '2008-01-01', 1.49)