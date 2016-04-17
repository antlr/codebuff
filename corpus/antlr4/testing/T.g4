grammar T;

foo : RAISERROR '(' msg=(DECIMAL | STRING | LOCAL_ID) ',' ;

delete_statement
   :   with_expression? DELETE (TOP '(' expression ')' PERCENT?)? FROM?
       (table_alias
 | ddl_object
                    |   rowset_function_limited
                    |   table_var= LOCAL_ID)
	with_table_hints? output_clause?
   ;
