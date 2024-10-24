/*
 * @(#)Lexer.java
 *
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

public final class Lexer {

    public static final char EOL = '\n';
    public static final char EOT = '\u0000';

    private final InputStream source;
    private       boolean    debug;

    private char         currentChar;
    private StringBuffer currentSpelling;
    private boolean      currentlyScanningToken;

    int                 currentLine;

    public Lexer(InputStream source) {
        this.source = source;

        currentChar = getSource();
        debug = false;
        currentLine = 1;
    }

    public static Lexer fromResource(String handle) {
        return new Lexer(Lexer.class.getResourceAsStream(handle));
    }

    public static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // isOperator returns true iff the given character is an operator character.

    public static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    ///////////////////////////////////////////////////////////////////////////////

    public static boolean isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '<' || c == '>' || c == '\\'
                || c == '&' || c == '@' || c == '%' || c == '^' || c == '?');
    }

    public void enableDebugging() {
        debug = true;
    }

    // takeIt appends the current character to the current token, and gets
    // the next character from the source program.

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
        if (debug) {
            System.out.println(tok);
        }
        return tok;
    }

    // scanSeparator skips a single separator.

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

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                do {
                    takeIt();
                } while (isLetter(currentChar) || isDigit(currentChar));
                return Token.Kind.IDENTIFIER;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    takeIt();
                } while (isDigit(currentChar));
                return Token.Kind.INTLITERAL;

            case '+':
            case '-':
            case '*':
            case '/':
            case '=':
            case '<':
            case '>':
            case '\\':
            case '&':
            case '@':
            case '%':
            case '^':
            case '?':
                do {
                    takeIt();
                } while (isOperator(currentChar));
                return Token.Kind.OPERATOR;

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

}
