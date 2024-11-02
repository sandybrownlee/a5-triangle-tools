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

package triangle.syntacticAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Lexer {

    // TODO: make configurable for '\r\n' or '\n'
    public static final char EOL = '\n';
    public static final char EOT = '\u0000';

    private static final List<Character>         operators     = List.of('+', '-', '*', '/', '=', '<', '>', '\\', '&', '@', '%',
                                                                         '^', '?', '|');
    private static final Map<String, Token.Kind> reservedWords = new HashMap<>();

    static {
        reservedWords.put("array", Token.Kind.ARRAY);
        reservedWords.put("begin", Token.Kind.BEGIN);
        reservedWords.put("const", Token.Kind.CONST);
        reservedWords.put("do", Token.Kind.DO);
        reservedWords.put("else", Token.Kind.ELSE);
        reservedWords.put("end", Token.Kind.END);
        reservedWords.put("func", Token.Kind.FUNC);
        reservedWords.put("if", Token.Kind.IF);
        reservedWords.put("in", Token.Kind.IN);
        reservedWords.put("let", Token.Kind.LET);
        reservedWords.put("loop", Token.Kind.LOOP);
        reservedWords.put("of", Token.Kind.OF);
        reservedWords.put("proc", Token.Kind.PROC);
        reservedWords.put("record", Token.Kind.RECORD);
        reservedWords.put("repeat", Token.Kind.REPEAT);
        reservedWords.put("then", Token.Kind.THEN);
        reservedWords.put("type", Token.Kind.TYPE);
        reservedWords.put("until", Token.Kind.UNTIL);
        reservedWords.put("var", Token.Kind.VAR);
        reservedWords.put("while", Token.Kind.WHILE);
    }

    private final InputStream  source;
    private final StringBuffer buffer;
    private       int          line;
    private       int          column;

    public Lexer(InputStream source) {
        this.source = source;
        this.line = 1;
        this.column = 1;
        this.buffer = new StringBuffer();
    }

    public static Lexer fromResource(String handle) {
        return new Lexer(Lexer.class.getResourceAsStream(handle));
    }

    Token nextToken() throws IOException {
        if (buffer.isEmpty()) {
            read();
        }

        Token toEmit = switch (lastChar()) {

            case Character c when Character.isWhitespace(c) -> {
                do {
                    read();
                } while (Character.isWhitespace(lastChar()));

                resetExceptLast();
                yield nextToken();
            }

            case '$' -> {
                // TODO: should block comments be stackable?
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
                int line = this.line;
                int column = this.column;

                read();

                if (lastChar() == '=') {
                    reset();
                    yield new Token(Token.Kind.BECOMES, line, column);
                }

                Token token = new Token(Token.Kind.COLON, line, column);
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
                int line = this.line;
                int column = this.column;

                do {
                    read();
                } while (Character.isDigit(lastChar()));

                Token token = new TextToken(Token.Kind.INTLITERAL, line, column, buffer.substring(0, buffer.length() - 1));
                resetExceptLast();
                yield token;
            }

            case '\'' -> {
                int line = this.line;
                int column = this.column;

                read();
                read();
                if (!(lastChar() == '\'')) {
                    yield new Token(Token.Kind.ERROR, line, column);
                }

                Token token = new TextToken(Token.Kind.CHARLITERAL, line, column, String.valueOf(buffer.charAt(1)));
                reset();
                yield token;
            }

            case Character c when Character.isLetter(c) -> {
                int line = this.line;
                int column = this.column;

                do {
                    read();
                } while (Character.isLetter(lastChar()) || Character.isDigit(lastChar()));

                // if the matched string is a reserved word, then create a token of the corresponding type
                String matchedString = buffer.substring(0, buffer.length() - 1);
                Token token = reservedWords.containsKey(matchedString) ?
                        new Token(reservedWords.get(matchedString), line, column) :
                        // else we have an identifier token
                        new TextToken(Token.Kind.IDENTIFIER, line, column, matchedString);

                resetExceptLast();
                yield token;
            }

            case Character c when operators.contains(c) -> {
                int line = this.line;
                int column = this.column;

                do {
                    read();
                } while (operators.contains(lastChar()));

                Token token = new TextToken(Token.Kind.OPERATOR, line, column, buffer.substring(0, buffer.length() - 1));
                resetExceptLast();
                yield token;
            }

            default -> {
                reset();
                yield new Token(Token.Kind.ERROR, line, column);
            }
        };

//        System.out.println(toEmit.getKind() + " " + (toEmit instanceof TextToken tt ? tt.getText() : "#") + " " + toEmit
//        .getLine() + "," + toEmit.getColumn());
        return toEmit;
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

    // TODO: Remove
    public void dump() throws IOException {
        Token tok = nextToken();

        if (tok.getKind() == Token.Kind.EOT) {
            return;
        }

        System.out.println(tok.getLine() + "," + tok.getColumn() + "\t\t"
                           + tok.getKind() + " " + (tok instanceof TextToken tt ? tt.getText() : "#"));
        dump();
    }

}
