/** Javadoc */
grammar Simple;

/** Javadoc */
noaltsRule : '(' b c ')' ('x' 'y')? ;

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
	|	(	a
		|	X
		|
		)
		start next line after list
	;

nested_nested
	:	(	A
			(	'lit'
			|	x
			)*
		)?
	;

