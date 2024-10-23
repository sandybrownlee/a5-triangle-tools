/*
 * @(#)Operator.java
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

package triangle.abstractSyntaxTrees.terminals;

import triangle.abstractSyntaxTrees.declarations.Declaration;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class Operator extends Terminal {

    public Declaration decl;

    public Operator(String spelling, SourcePosition position) {
        super(spelling, position);
        decl = null;
    }

    public <TArg, TResult> TResult visit(OperatorVisitor<TArg, TResult> v, TArg arg) {
        return v.visitOperator(this, arg);
    }

    public <TArg, TResult> TResult visit(OperatorVisitor<TArg, TResult> visitor) {
        return visit(visitor, null);
    }

}
