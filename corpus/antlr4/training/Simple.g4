/** Javadoc */
grammar Simple;

/** Javadoc */
noaltsRule : '(' b c ')' ('x' 'y')? ;

list_of_nontrivial_stuff
	:	A B
	|	x (',' x)*
	|	~A
	;

separator_pattern
	:	a (',' a)*
	;

terminator_pattern
	:	(stat ';')*
	;

single_alt_with_stuff
	:	'this' (A)? B (',' B)*
	;

optional_semi
	:	stat ';'? (';')?
	;

subexpresssions
	:	'do'
		(	A
		|	b
		)*
	|	again
		(	a
		|	'end'
		)+
	|	(	(	a
			|	b
			)
		|	X
		|
		)+
		start next line after list
	;

block
	:	'{'
		(	a
		|	X
		)+
		'}'
	;

nested_nested
	:	'{'
		(	A
			(	'lit'
			|	x
			)*
		)?
		'}'
	;

even_more_nested
   :	'@' X
		(	'('
			(	v
			|	w
			)?
			')'
		)?
   ;
