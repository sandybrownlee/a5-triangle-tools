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

import java.io.InputStream;

// TODO: logging
public final class Lexer {

    // TODO: make configurable for '\r\n' or '\n'
    public static final char EOL = '\n';
    public static final char EOT = '\u0000';

    private final InputStream source;
    int currentLine;
    private char         currentChar;
    private StringBuffer currentSpelling;
    private boolean      currentlyScanningToken;

    public Lexer(InputStream source) {
        this.source = source;

        currentChar = getSource();
        currentLine = 1;
    }

    public static Lexer fromResource(String handle) {
        return new Lexer(Lexer.class.getResourceAsStream(handle));
    }

    public static boolean isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '<' || c == '>' || c == '\\'
                || c == '&' || c == '@' || c == '%' || c == '^' || c == '?');
    }

    Token scan() {
        Token tok;
        SourcePosition pos;
        Token.Kind kind;

        currentlyScanningToken = false;
        // skip any whitespace or comments
        while (currentChar == '!' || currentChar == ' ' || currentChar == '\n' || currentChar == '\r'
               || currentChar == '\t') {
            scanSeparator();
        }

        currentlyScanningToken = true;
        currentSpelling = new StringBuffer();
        pos = new SourcePosition();
        pos.setStart(currentLine);

        kind = scanToken();

        pos.setFinish(currentLine);
        tok = new Token(kind, currentSpelling.toString(), pos);
        return tok;
    }

    char getSource() {
        try {
            int c = source.read();

            if (c == -1) {
                c = EOT;
            } else if (c == EOL) {
                currentLine++;
            }
            return (char) c;
        } catch (java.io.IOException s) {
            return EOT;
        }
    }

    private void takeIt() {
        if (currentlyScanningToken) {
            currentSpelling.append(currentChar);
        }
        currentChar = getSource();
    }

    private void scanSeparator() {
        switch (currentChar) {

            // comment
            case '!':

                // the comment ends when we reach an end-of-line (EOL) or end of file (EOT - for end-of-transmission)
                do {
                    takeIt();
                } while ((currentChar != EOL) && (currentChar != EOT));
                if (currentChar == EOL) {
                    takeIt();
                }
                break;

            // whitespace
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                takeIt();
                break;
        }
    }

    private Token.Kind scanToken() {

        switch (currentChar) {

            case Character c when Character.isLetter(c): {
                do {
                    takeIt();
                } while (Character.isLetter(currentChar) || Character.isDigit(currentChar));

                return Token.Kind.IDENTIFIER;
            }

            case Character c when Character.isDigit(c): {
                do {
                    takeIt();
                } while (Character.isDigit(currentChar));

                return Token.Kind.INTLITERAL;
            }

            case Character c when isOperator(c): {
                do {
                    takeIt();
                } while (isOperator(currentChar));
                return Token.Kind.OPERATOR;
            }

            case '\'':
                takeIt();
                takeIt(); // the quoted character
                if (currentChar == '\'') {
                    takeIt();
                    return Token.Kind.CHARLITERAL;
                } else {
                    return Token.Kind.ERROR;
                }

            case '.':
                takeIt();
                return Token.Kind.DOT;

            case ':':
                takeIt();
                if (currentChar == '=') {
                    takeIt();
                    return Token.Kind.BECOMES;
                } else {
                    return Token.Kind.COLON;
                }

            case ';':
                takeIt();
                return Token.Kind.SEMICOLON;

            case ',':
                takeIt();
                return Token.Kind.COMMA;

            case '~':
                takeIt();
                return Token.Kind.IS;

            case '(':
                takeIt();
                return Token.Kind.LPAREN;

            case ')':
                takeIt();
                return Token.Kind.RPAREN;

            case '[':
                takeIt();
                return Token.Kind.LBRACKET;

            case ']':
                takeIt();
                return Token.Kind.RBRACKET;

            case '{':
                takeIt();
                return Token.Kind.LCURLY;

            case '}':
                takeIt();
                return Token.Kind.RCURLY;

            case EOT:
                return Token.Kind.EOT;

            default:
                takeIt();
                return Token.Kind.ERROR;
        }
    }

    record Token(Kind kind, String text, SourcePosition position) {

        Token(Kind kind, String text, SourcePosition position) {

            // If this token is an identifier, is it also a reserved word?
            if (kind == Kind.IDENTIFIER) {
                this.kind = Kind.fromSpelling(text);
            } else {
                this.kind = kind;
            }

            this.text = text;
            this.position = position;
        }

        public static String spell(Kind kind) {
            return kind.spelling;
        }

        @Override
        public String toString() {
            return "Kind=" + kind + ", text=" + text + ", position=" + position;
        }

        // Token classes...

        public enum Kind {
            // literals, identifiers, operators...
            INTLITERAL("<int>"), CHARLITERAL("<char>"), IDENTIFIER("<identifier>"), OPERATOR("<operator>"),

            // reserved words - keep in alphabetical order for ease of maintenance...
            ARRAY("array"), BEGIN("begin"), CONST("const"), DO("do"), ELSE("else"), END("end"), FUNC("func"), IF("if"), IN("in"),
            LET("let"), OF("of"),
            PROC("proc"), RECORD("record"), THEN("then"), TYPE("type"), VAR("var"), WHILE("while"),

            // punctuation...
            DOT("."), COLON(":"), SEMICOLON(";"), COMMA(","), BECOMES(":="), IS("~"),

            // brackets...
            LPAREN("("), RPAREN(")"), LBRACKET("["), RBRACKET("]"), LCURLY("{"), RCURLY("}"),

            // special tokens...
            EOT(""), ERROR("<error>");

            private final static Kind firstReservedWord = ARRAY, lastReservedWord = WHILE;
            public final String spelling;

            Kind(String spelling) {
                this.spelling = spelling;
            }

            /**
             iterate over the reserved words above to find the one with a given text
             need to specify firstReservedWord and lastReservedWord (inclusive) for this
             to work!

             @return Kind.IDENTIFIER if no matching token class found
             */
            public static Kind fromSpelling(String spelling) {
                boolean isRW = false;
                for (Kind kind : Kind.values()) {
                    if (kind == firstReservedWord) {
                        isRW = true;
                    }

                    if (isRW && kind.spelling.equals(spelling)) {
                        return kind;
                    }

                    if (kind == lastReservedWord) {
                        // if we get here, we've not found a match, so break and return failure
                        break;
                    }
                }
                return Kind.IDENTIFIER;
            }
        }

    }

}
