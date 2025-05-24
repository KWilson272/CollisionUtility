package me.kwilson272.collisionutility.parser.token;

public enum TokenType {
    // Keywords
    KEY_GROUP,

    // Operators
    GREATER_THAN, LESS_THAN, EQUAL, TILDE, X,

    // Non-Operator Terminals
    QUOTE, OPEN_BRACE, CLOSE_BRACE, COMMA, POUND,
    DOLLAR_SIGN, STRING_LIT, EOL, EOF,
}
