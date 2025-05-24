# Collision Utility Grammar
This class outlines the formal grammar for the CollisionUtility language.
If you are an end-user, this document is of no use to you. 

### Notation 
* **`<non_terminal>`**: Represents a production rule in the grammar. (e.g. <expression>)
* **`TERMINAL`**: Represents a token type produced by the lexer. (e.g. STRING_LITERAL)
* **`::=`**: "Is defined as." - To separate a production rule from what it produces
* **`|`**: "Or." - used when a production rule can result in different productions
* **`?`**: Not an explicit grammar component, rather it indicates an element is optional

### Grammar rules:

```bnf
<line> ::=
    <expression> EOL
    | EOL
    | EOF
    
<expression> ::=
    <group_declaration>
    | <collision_declaration>

<group_declaration> ::=
    KEY_GROUP QUOTE STRING_LITERAL QUOTE OPEN_BRACE <operand_list>? CLOSE_BRACE

<operand_list> ::=
    <operand>
    | <operand> COMMA <operand_list>

<operand> ::=
    <group_reference>
    | STRING_LITERAL

<group_reference> ::=
    DOLLAR_SIGN STRING_LITERAL

<collision_declaration> ::=
    <operand> <operator> <operand>

<operator> ::=
    GREATER_THAN
    | LESS_THAN
    | EQUAL
    | TILDE
    | X
```