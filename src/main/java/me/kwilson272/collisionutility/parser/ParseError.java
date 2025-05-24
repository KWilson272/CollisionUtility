package me.kwilson272.collisionutility.parser;

import me.kwilson272.collisionutility.parser.token.Token;

/**
 * Small exception class to denote the parser finding Invalid Syntax
 */
public class ParseError extends Exception {

    public ParseError(String errorMessage, Token token) {
        super("ERROR: Unexpected token " + token.type() + " found on line: "
                + token.lineNumber() + "\n" + errorMessage);
    }
}
