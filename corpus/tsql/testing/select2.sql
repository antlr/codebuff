--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- Creating tables with SELECT INTO

IF OBJECT_ID (N'#Bicycles',N'U') IS NOT NULL
DROP TABLE #Bicycles;

SELECT * 
INTO #Bicycles
FROM AdventureWorks2012.Production.Product
WHERE ProductNumber LIKE 'BK%';

USE AdventureWorks2012;
IF OBJECT_ID('dbo.NewProducts', 'U') IS NOT NULL
    DROP TABLE dbo.NewProducts;

SELECT * INTO dbo.NewProducts
FROM Production.Product
WHERE ListPrice > $25 
AND ListPrice < $100;

