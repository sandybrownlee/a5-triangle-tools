/*
 * @(#)RecordExpression.java
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

package triangle.abstractSyntaxTrees.expressions;

import triangle.abstractSyntaxTrees.aggregates.RecordAggregate;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class RecordExpression extends Expression {

    public final RecordAggregate RA;

    public RecordExpression(RecordAggregate raAST, SourcePosition position) {
        super(position);
        RA = raAST;
    }

    public <TArg, TResult> TResult visit(ExpressionVisitor<TArg, TResult> v, TArg arg) {
        return v.visitRecordExpression(this, arg);
    }

}
