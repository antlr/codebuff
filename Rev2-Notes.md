# CodeBuff Second implementation

*Document in progress*

The first version looked at a window of tokens and rule context information to predict: injecting newlines, injecting whitespace, alignment, and indentation. It works surprisingly well but it makes no guarantees that things will line up properly. For example we had to add a new feature so that {...} had both curlies on the same line if the statements in the block were on the same line.

The new approach is to look at subtree structures and identify the most common patterns.

## Token dependencies

Here is an example where we want the `}` to line up with the `void`, but those tokens are in a subtree. On the other hand, we can always ask whether or not the last token for a subtree, `}` here, aligns with another token.

```java
void f(int i, int j) {
}
```

<img src="images/method-def.png" width=400>

| Features      | Prediction |
| ------------- |:-------------:|
|(methodDeclaration, void, ID) | none (same line)|
|(methodDeclaration, void, }) | align|
|(methodDeclaration, ID, }) | none (diff line)|

We could start with that modest goal. It would work for ANTLR too.

```
a : x
  | y
  ;
```

<img src="images/rule.png" width=250>

| Features      | Prediction |
| ------------- |:-------------:|
|(parserRuleSpec, ID, :) | none (same line)|
|(parserRuleSpec, ID, ;) | none (diff line)|
|(parserRuleSpec, :, ;) | align|

If everything were on one line, then there would be no alignment trained (or predicted, hopefully):

```
a : x | y ;
```

There could be multiple alignments. For example, with SQL, we might see three tokens aligned:

```sql
SELECT
	NAME, ID
FROM
	USERS
;
```

| Features      | Prediction |
| ------------- |:-------------:|
|(selectStmt, SELECT, FROM) | align|
|(selectStmt, SELECT, ;) | align|
|(selectStmt, FROM, ;) | align|

## Lists of elements

We not only have to line up certain tokens, but lists of elements are often aligned. Lists can be defined in two ways:

* Direct siblings with the same rule name
* First token of direct siblings are aligned (`switch` subtrees are `statement`s with `switchBlockStatementGroup` for cases and `switchLabel` for `default` so they have different names but first tokens are aligned)

Formatting lists has a few key patterns:

* Are the elements aligned or not (first token of each element)
* If aligned, are the elements first on line or is the separator first? This dictates whether or not the newline gets injected before or after the separator.
* Is the first element on a line by itself? If so, indented?

For Java, such as:

```java
{
	int i;
	x=y;
}
```

<img src="images/method-body.png" width=400>

We get:

| Features      | Prediction |
| ------------- |:-------------:|
| (block,blockStatement) | aligned, \n before first el, indent first el, no sep |
| | |
| (block,{,}) | align |

For the antlr example again, we have a problem:

```
a : x
  | y		// the '|' is indented let's say; not aligned
  ;
```

Let's assume we can only align the elements, not the separators, and can only align on first tokens of a line. If not aligned, we can still indent. So, we get

| Features      | Prediction |
| ------------- |:-------------:|
|(ruleAltList, labeledAlt) | unaligned, no \n before first el, \n before sep, indent following|

Here, `indent following` means that each element is indented, except for perhaps the first one if it has no newline before it.

I've seen people make rules like the following:

```
biggy :
	x |
	y |
	z
	;
```

we get

| Features      | Prediction |
| ------------- |:-------------:|
|(ruleAltList, labeledAlt) | aligned, \n before first el, indent first el, \n after sep|

Looking at the formal method arg list above but with new formatting:

```java
void f(int i,
       int j) 
{
}
```

we get

| Features      | Prediction |
| ------------- |:-------------:|
|(formalParameterList,formalParameter) | aligned, no \n before first el, \n after sep|

```java
void f(
	int i,
	int j) 
{
}
```

we get

| Features      | Prediction |
| ------------- |:-------------:|
|(formalParameterList,formalParameter) | aligned, \n before first el, indent first el, \n after sep|

*If things are not aligned then we assume they all go on the same line.*

## Indent

Instead of keeping a current indent level and then having to worry about how much to dedent at the end of a list, it's probably better to think about this in a functional way. We inject newlines and indentation but that is all relative to the ancestors of the current tree. It could be that the current code block subtree is nested deeply within some function. All we care about is whether or not we indent the current block's first element.  We don't have to undo some `currentIndent` state variable to the indent level of the immediate ancestor.

For example, in a Java switch statement, the dedent is like 3x on a `default` clause without statements:
 
```java
switch ( x ) {
	case 0 :
		break;
	case 1 :
		break;
	default :
}
```

<img src="images/switch.png" width=800>

| Features      | Prediction |
| ------------- |:-------------:|
| (statement,switchBlockStatementGroup/switchLabel) | aligned, \n before first el, no separator, indented |
| | |
| (statement,switch,{) | none (same line) |
| (statement,switch,}) | align |
| (statement,{,}) | none (diff line)|

## Whitespace between tokens
