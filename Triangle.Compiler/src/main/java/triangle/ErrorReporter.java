/*
 * @(#)ErrorReporter.java
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

package triangle;

import triangle.syntacticAnalyzer.SourcePositionOld;

@Deprecated public class ErrorReporter {

    private final boolean throwExceptions;
    private       int     numErrors;

    /**
     @param throwExceptions if true, throw exceptions (good for unit tests) otherwise write to stdout
     */
    public ErrorReporter(boolean throwExceptions) {
        numErrors = 0;
        this.throwExceptions = throwExceptions;
    }

    public void reportError(String message, SourcePositionOld pos) {

        numErrors++;

        StringBuffer s = new StringBuffer("ERROR: ");

        s.append(message);
        s.append(" ").append(pos.getStart()).append("..").append(pos.getFinish());

        if (throwExceptions) {
            throw new RuntimeException(s.toString());
        } else {
            System.out.println(s);
        }
    }

    public void reportRestriction(String message) {
        System.out.println("RESTRICTION: " + message);
    }

    public int getNumErrors() {
        return numErrors;
    }

}
