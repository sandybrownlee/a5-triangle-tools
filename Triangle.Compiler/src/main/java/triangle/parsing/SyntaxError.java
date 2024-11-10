/*
 * @(#)SyntaxError.java
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

public class SyntaxError extends Exception {

    SyntaxError(Token token, Token.Kind expected) {
        super("expected: " + expected + " got: " + token + " at line " + token.getLine() + ", column " + token.getColumn());
    }

    SyntaxError(Token token) {
        super("unexpected token: " + token + " at line " + token.getLine() + ", column " + token.getColumn());
    }

}
