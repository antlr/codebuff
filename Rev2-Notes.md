# CodeBuff Second implementation

The first version looked at a window of tokens and rule context information to predict: injecting newlines, injecting whitespace, alignment, and indentation. It works surprisingly well but it makes no guarantees that things will line up properly. For example we had to add a new feature so that {...} had both curlies on the same line if the statements in the block were on the same line.

## Subtree patterns

The new approach is to look at subtree structures and identify the most common patterns.

### Token dependencies

Here is an example where we want the `}` to line up with the `void`, but those tokens are in a subtree. On the other hand, we can always ask whether or not the last token for a subtree, `}` here, aligns with another token.

```java
void f(int i, int j) {
}
```

<img src="images/method-def.png" width=700>

| Features      | Prediction |
| ------------- |:-------------:|
|(methodDeclaration, void, ID) | none|
|(methodDeclaration, void, }) | align|
|(methodDeclaration, ID, }) | none|

We could start with that modest goal. It would work for ANTLR too.

```
a : x
  | y
  ;
```

<img src="images/rule.png" width=250>

| Features      | Prediction |
| ------------- |:-------------:|
|(parserRuleSpec, ID, :) | none|
|(parserRuleSpec, ID, ;) | none|
|(parserRuleSpec, :, ;) | align|

If everything were on one line, then there would be no alignment trained (or predicted, hopefully).

There could be multiple alignments. For example, with SQL, we might see three tokens aligned:

```sql
SELECT
	NAME, ID
FROM
	USERS
;
```

But, we would only detect the middle one if both `SELECT` and `FROM` were direct siblings.

| Features      | Prediction |
| ------------- |:-------------:|
|(selectStmt, SELECT, FROM) | align|
|(selectStmt, SELECT, ;) | align|
|(selectStmt, FROM, ;) | align|

### Lists of elements


```java
{
	int i;
	x=y;
}
```

<img src="images/method-body.png" width=700>

### Indent

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

