-----------------------------------------------------------------------
-- Close https://msdn.microsoft.com/en-us/library/ms175035.aspx
-- CLOSE { { [ GLOBAL ] cursor_name } | cursor_variable_name }

DECLARE Employee_Cursor CURSOR FOR
SELECT EmployeeID, Title FROM AdventureWorks2012.HumanResources.Employee;
OPEN Employee_Cursor;
FETCH NEXT FROM Employee_Cursor;
WHILE @@FETCH_STATUS = 0
   BEGIN
      FETCH NEXT FROM Employee_Cursor;
   END;
CLOSE Employee_Cursor;
DEALLOCATE Employee_Cursor;
GO

