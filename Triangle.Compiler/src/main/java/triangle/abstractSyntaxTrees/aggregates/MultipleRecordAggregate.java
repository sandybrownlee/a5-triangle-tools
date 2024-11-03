/*
 * @(#)MultipleRecordAggregate.java
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

package triangle.abstractSyntaxTrees.aggregates;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.syntacticAnalyzer.SourcePositionOld;

@Deprecated public class MultipleRecordAggregate extends RecordAggregate {

    public final Identifier      I;
    public final RecordAggregate RA;
    public       Expression      E;

    public MultipleRecordAggregate(Identifier iAST, Expression eAST, RecordAggregate raAST, SourcePositionOld position) {
        super(position);
        I = iAST;
        E = eAST;
        RA = raAST;
    }

    public <TArg, TResult> TResult visit(RecordAggregateVisitor<TArg, TResult> v, TArg arg) {
        return v.visitMultipleRecordAggregate(this, arg);
    }

}
