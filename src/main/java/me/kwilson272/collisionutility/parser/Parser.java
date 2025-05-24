package me.kwilson272.collisionutility.parser;

import me.kwilson272.collisionutility.parser.grammar.CollisionDeclaration;
import me.kwilson272.collisionutility.parser.grammar.EmptyExpression;
import me.kwilson272.collisionutility.parser.grammar.Expression;
import me.kwilson272.collisionutility.parser.grammar.GroupDeclaration;
import me.kwilson272.collisionutility.parser.grammar.Operand;
import me.kwilson272.collisionutility.parser.token.Token;
import me.kwilson272.collisionutility.parser.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Recursive descent parser to convert a list of tokens into a list of
 * expressions, with error checking.
 */
public class Parser {

    private final List<Token> tokens;
    private final Logger logger;

    private int cursor;

    /**
     * Creates a new parser to create expressions out of tokens
     *
     * @param tokens the List of tokens to be parsed
     * @param logger the logger used when errors are encountered
     */
    public Parser(List<Token> tokens, Logger logger) {
        this.tokens = tokens;
        this.logger = logger;
        this.cursor = 0;
    }

    private Token peek() {
        return tokens.get(cursor);
    }

    private void advance() {
        if (cursor < tokens.size() - 1) {
            ++cursor;
        }
    }

    private Token previous() {
        return tokens.get(cursor-1);
    }

    private boolean check(TokenType tokenType) {
        return peek().type() == tokenType;
    }

    private void consume(TokenType type, String errorMessage) throws ParseError {
        if (check(type)) {
            advance();
            return;
        }
        throw new ParseError(errorMessage, peek());
    }

    private boolean match(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * @return all valid expressions derived from the token list.
     */
    public List<Expression> parseExpressions() {
        List<Expression> expressions = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            try {
                expressions.add(line());
            } catch (ParseError e) {
                logger.log(Level.WARNING, e.getMessage());
                // Given this language works on a line by line basis, it is expected
                // that one bad line shouldn't ruin every other line. Thus, we will
                // try and discard the current line to 'reset' the parser

                // if the EOL/EOF is the invalid token, we don't have to worry about
                // consuming a non-offending line
                while (!check(TokenType.EOF) && !check(TokenType.EOL)) {
                    advance();
                }
            }
        }
        return expressions;
    }

    private Expression line() throws ParseError {
        if (match(TokenType.EOL, TokenType.POUND)) {
            return new EmptyExpression();
        } else if (check(TokenType.EOF)) {
            // Do not advance so we can allow our while loop to terminate
            return new EmptyExpression();
        } else {
            Expression expr = expression();
            if (!match(TokenType.EOL, TokenType.EOF, TokenType.POUND)) {
                throw new ParseError("Expected a new line, end of file, or comment " +
                        "marker at the end of a complete expression", peek());
            }
            return expr;
        }
    }

    private Expression expression() throws ParseError {
        TokenType current = peek().type();
        if (current == TokenType.STRING_LIT || current == TokenType.DOLLAR_SIGN) {
            return collisionDeclaration();
        } else if (current == TokenType.KEY_GROUP) {
            return groupDeclaration();
        } else {
            throw new ParseError("Expected a collision declaration or group declaration", peek());
        }
    }

    private GroupDeclaration groupDeclaration() throws ParseError {
        consume(TokenType.KEY_GROUP, "Group declarations must start with the keyword 'group'");
        consume(TokenType.QUOTE, "Group names must be specified in quotations, missing openning '\"'");
        String groupName = stringLiteral();
        consume(TokenType.QUOTE, "Group names must be specified in quotations, missing closing '\"'");
        consume(TokenType.OPEN_BRACE, "Group contents must be declared in braces, missing '{'");
        List<Operand> groupContents = operandList();
        consume(TokenType.CLOSE_BRACE, "Group contents must be declared in braces, missing '}'");
        return new GroupDeclaration(groupName, groupContents);
    }

    String stringLiteral() throws ParseError {
        consume(TokenType.STRING_LIT, "Expected a String literal.");
        return previous().literal();
    }

    List<Operand> operandList() throws ParseError {
        List<Operand> operands = new ArrayList<>();
        if (check(TokenType.CLOSE_BRACE)) {
            return operands;
        }
        do {
            operands.add(operand());
        } while (match(TokenType.COMMA));
        return operands;
    }

    private Operand operand() throws ParseError {
        if (check(TokenType.DOLLAR_SIGN)) {
            advance();
            return new Operand(true, stringLiteral(), peek().lineNumber());
        }
        return new Operand(false, stringLiteral(), peek().lineNumber());
    }

    private CollisionDeclaration collisionDeclaration() throws ParseError {
        return new CollisionDeclaration(operand(), operator(), operand());
    }

    private Token operator() throws ParseError {
        if (match(TokenType.GREATER_THAN, TokenType.LESS_THAN,
                TokenType.EQUAL, TokenType.TILDE, TokenType.X)) {
           return previous();
        }
        throw new ParseError("An operator is required in collision declarations." +
                " Use: >, <, =, ~, or X. See the github documentation for details", peek());
    }
}
