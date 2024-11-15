/*
 * @(#)Lexer.java
 *
 * Revisions and updates (c) 2024 Zaraksh Rahman. zar00024@students.stir.ac.uk
 * Revisions and updates (c) 2022-2024 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 *
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Lexer {

    public static final char EOL = '\n';
    public static final char EOT = '\u0000';

    //@formatter:off
    private static final List<Character>         operators     = List.of(
            '+', '-', '*', '/', '=', '<', '>', '\\', '&', '@', '%', '^', '?', '|'
    );
    private static final Map<String, Token.Kind> reservedWords = new HashMap<>();

    static {
        reservedWords.put("after", Token.Kind.AFTER);
        reservedWords.put("array", Token.Kind.ARRAY);
        reservedWords.put("begin", Token.Kind.BEGIN);
        reservedWords.put("const", Token.Kind.CONST);
        reservedWords.put("do", Token.Kind.DO);
        reservedWords.put("else", Token.Kind.ELSE);
        reservedWords.put("end", Token.Kind.END);
        reservedWords.put("false", Token.Kind.FALSE);
        reservedWords.put("func", Token.Kind.FUNC);
        reservedWords.put("if", Token.Kind.IF);
        reservedWords.put("in", Token.Kind.IN);
        reservedWords.put("let", Token.Kind.LET);
        reservedWords.put("loop", Token.Kind.LOOP);
        reservedWords.put("of", Token.Kind.OF);
        reservedWords.put("proc", Token.Kind.PROC);
        reservedWords.put("record", Token.Kind.RECORD);
        reservedWords.put("repeat", Token.Kind.REPEAT);
        reservedWords.put("return", Token.Kind.RETURN);
        reservedWords.put("then", Token.Kind.THEN);
        reservedWords.put("true", Token.Kind.TRUE);
        reservedWords.put("type", Token.Kind.TYPE);
        reservedWords.put("until", Token.Kind.UNTIL);
        reservedWords.put("var", Token.Kind.VAR);
        reservedWords.put("while", Token.Kind.WHILE);
    }
    //@formatter:on

    public static Lexer fromResource(String handle) {
        return new Lexer(Lexer.class.getResourceAsStream(handle));
    }

    private final InputStream  source;
    private final StringBuffer buffer;
    private       int          line;
    private       int          column;

    Lexer(InputStream source) {
        this.source = source;
        this.line = 1;
        this.column = 1;
        this.buffer = new StringBuffer();
    }

    Token nextToken() throws IOException {
        if (buffer.isEmpty()) {
            read();
        }

        return switch (lastChar()) {

            case Character c when Character.isWhitespace(c) -> {
                do {
                    read();
                } while (Character.isWhitespace(lastChar()));

                resetExceptLast();
                yield nextToken();
            }

            // TODO: should block comments be nestable?
            case '$' -> {
                do {
                    read();
                } while (lastChar() != '$');

                // no need to put '$' back into the buffer
                reset();
                yield nextToken();
            }

            case '!', '#' -> {
                do {
                    read();
                } while (lastChar() != EOL);

                // no need to put EOL back into the buffer
                reset();
                yield nextToken();
            }

            case EOT -> new Token(Token.Kind.EOT, line, column);

            case '(' -> {
                reset();
                yield new Token(Token.Kind.LPAREN, line, column);
            }
            case ')' -> {
                reset();
                yield new Token(Token.Kind.RPAREN, line, column);
            }
            case '[' -> {
                reset();
                yield new Token(Token.Kind.LBRACK, line, column);
            }
            case ']' -> {
                reset();
                yield new Token(Token.Kind.RBRACK, line, column);
            }
            case '{' -> {
                reset();
                yield new Token(Token.Kind.LBRACE, line, column);
            }
            case '}' -> {
                reset();
                yield new Token(Token.Kind.RBRACE, line, column);
            }
            case '.' -> {
                reset();
                yield new Token(Token.Kind.DOT, line, column);
            }

            case ':' -> {
                int line1 = this.line;
                int column1 = this.column;

                read();

                if (lastChar() == '=') {
                    reset();
                    yield new Token(Token.Kind.BECOMES, line1, column1);
                }

                Token token = new Token(Token.Kind.COLON, line1, column1);
                resetExceptLast();
                yield token;
            }

            case ';' -> {
                reset();
                yield new Token(Token.Kind.SEMICOLON, line, column);
            }
            case ',' -> {
                reset();
                yield new Token(Token.Kind.COMMA, line, column);
            }
            case '~' -> {
                reset();
                yield new Token(Token.Kind.IS, line, column);
            }

            case Character c when Character.isDigit(c) -> {
                int line1 = this.line;
                int column1 = this.column;

                do {
                    read();
                } while (Character.isDigit(lastChar()));

                Token token = new TextToken(Token.Kind.INTLITERAL, line1, column1, buffer.substring(0, buffer.length() - 1));
                resetExceptLast();
                yield token;
            }

            case '\'' -> {
                int line1 = this.line;
                int column1 = this.column;

                read();
                read();
                if (!(lastChar() == '\'')) {
                    yield new Token(Token.Kind.ERROR, line1, column1);
                }

                Token token = new TextToken(Token.Kind.CHARLITERAL, line1, column1, String.valueOf(buffer.charAt(1)));
                reset();
                yield token;
            }

            case Character c when Character.isLetter(c) -> {
                int line1 = this.line;
                int column1 = this.column;

                do {
                    read();
                } while (Character.isLetter(lastChar()) || Character.isDigit(lastChar()));

                // if the matched string is a reserved word, then create a token of the corresponding type signature
                String matchedString = buffer.substring(0, buffer.length() - 1);
                Token token = reservedWords.containsKey(matchedString) ? new Token(
                        reservedWords.get(matchedString), line1, column1) :
                        // else we have an identifier token
                        new TextToken(Token.Kind.IDENTIFIER, line1, column1, matchedString);

                resetExceptLast();
                yield token;
            }

            case Character c when operators.contains(c) -> {
                int line1 = this.line;
                int column1 = this.column;

                do {
                    read();
                } while (operators.contains(lastChar()));

                Token token = new TextToken(Token.Kind.OPERATOR, line1, column1, buffer.substring(0, buffer.length() - 1));
                resetExceptLast();
                yield token;
            }

            default -> {
                reset();
                yield new Token(Token.Kind.ERROR, line, column);
            }
        };
    }

    void read() throws IOException {
        int c = source.read();

        if (c == -1) {
            source.close();
            buffer.append(EOT);
            return;
        }

        if ((char) c == EOL) {
            line++;
            column = 0;
        } else {
            column++;
        }

        buffer.append((char) c);
    }

    private char lastChar() {
        return buffer.charAt(buffer.length() - 1);
    }

    private void reset() {
        buffer.setLength(0);
    }

    private void resetExceptLast() {
        char lastRead = lastChar();
        reset();
        buffer.append(lastRead);
    }

}
