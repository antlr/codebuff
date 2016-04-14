grammar Simple;

/** Javadoc */
noaltsRule : '(' b c ')' ;

list_of_nontrivial_stuff
	:	A B
	|	x (',' x)*
	|	~A
	;

single_alt_with_stuff
	:	'this' (A)? B (',' B)*
	;

subexpresssions
	:	'do'
		(	A
		|	b
		)*
	|	again
		(	a
		|	X
		)+
	;

nested_nested
	:	(	A
			(	'lit'
			|	x
			)*
		)?
	;

