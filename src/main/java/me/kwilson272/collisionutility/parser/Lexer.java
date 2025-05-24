package me.kwilson272.collisionutility.parser;

import me.kwilson272.collisionutility.parser.token.Token;
import me.kwilson272.collisionutility.parser.token.TokenType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads string input and converts it into tokens (tokenizes).
 */
public class Lexer {

    private final File source;
    private final Logger logger;
    private final Map<Character, TokenType> specialChars;

    private String line;
    private int cursor;
    private int lineNumber;

    /**
     * Creates a new Lexer object to tokenize the provided source file.
     *
     * @param source the File to be tokenized
     * @param logger the Logger used when errors are encountered
     */
    public Lexer(File source, Logger logger) {
        this.source = source;
        this.logger = logger;

        specialChars = new HashMap<>();
        specialChars.put('#', TokenType.POUND);
        specialChars.put('"', TokenType.QUOTE);
        specialChars.put('{', TokenType.OPEN_BRACE);
        specialChars.put('}', TokenType.CLOSE_BRACE);
        specialChars.put(',', TokenType.COMMA);
        specialChars.put('>', TokenType.GREATER_THAN);
        specialChars.put('<', TokenType.LESS_THAN);
        specialChars.put('=', TokenType.EQUAL);
        specialChars.put('x', TokenType.X);
        specialChars.put('~', TokenType.TILDE);
        specialChars.put('$', TokenType.DOLLAR_SIGN);

        line = "";
        cursor = 0;
        lineNumber = 0;
    }

    /**
     * Converts the source file into a list of parsable tokens.
     *
     * @return a List of tokens ordered such that the first token read
     * from the file is the first in the returned collection.
     */
    public List<Token> readTokens() {
        List<Token> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {
            while (true) {
                Token token = nextToken();
                tokens.add(token);
                switch (token.type()) {
                    case POUND: // A comment, we want to ignore until the next line
                        // Additionally, we want to pass an EOL to ensure that incomplete lines
                        // are caught, and additionally that we don't accidentally concatenate
                        // two lines
                        tokens.add(new Token(TokenType.EOL, "", lineNumber));
                        // Fall through because the code should be the same
                    case EOL:
                        line = reader.readLine();
                        cursor = 0;
                        lineNumber++;
                        break;
                    case EOF:
                        return tokens;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            String error = "An error occurred while attempting to read from the file '%s'%n";
            logger.log(Level.WARNING, String.format(error, source.getAbsolutePath()));
        }
        return tokens;
    }

    /**
     * Retrieves and constructs the next token from the input line.
     * @return a Token
     */
    private Token nextToken() {
        if (line == null) { // Should only be null when end of file is reached
            return new Token(TokenType.EOF, "", lineNumber);
        }

        eatWhiteSpace();
        if (cursor == line.length()) { // empty line or no more remaining text
            return new Token(TokenType.EOL, "", lineNumber);
        }

        char c = line.charAt(cursor);
        if (specialChars.containsKey(c)) {
            cursor++;
            return new Token(specialChars.get(c), String.valueOf(c), lineNumber);
        }

        String literal = nextString();
        TokenType type = literal.equalsIgnoreCase("group") ? TokenType.KEY_GROUP :
                                                   TokenType.STRING_LIT;
        return new Token(type, literal, lineNumber);
    }

    /**
     * Advances the cursor to the beginning of the next non-whitespace
     * string.
     */
    private void eatWhiteSpace() {
        while (cursor < line.length()
                && Character.isWhitespace(line.charAt(cursor))) {
            cursor++;
        }
    }

    /**
     * @return the next substring of the line that does not contain
     * any whitespace or special characters
     */
    private String nextString() {
        int start = cursor;
        while(cursor < line.length()
                && !specialChars.containsKey(line.charAt(cursor))
                && !Character.isWhitespace(line.charAt(cursor))) {
            cursor++;
        }
        return line.substring(start, cursor);
    }
}
