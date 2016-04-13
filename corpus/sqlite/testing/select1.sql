SELECT ProductModelID, Name
FROM Production.ProductModel
WHERE ProductModelID IN (3, 4);

SELECT ProductModelID, Name
FROM Production.ProductModel
WHERE ProductModelID NOT IN (3, 4)
UNION
SELECT ProductModelID, Name
FROM dbo.Gloves
ORDER BY Name;
