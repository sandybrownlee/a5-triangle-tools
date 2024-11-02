/*
 * @(#)MultipleArrayAggregate.java
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
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

@Deprecated public class MultipleArrayAggregate extends ArrayAggregate {

    public final ArrayAggregate AA;
    public       Expression     E;

    public MultipleArrayAggregate(Expression eAST, ArrayAggregate aaAST, SourcePosition position) {
        super(position);
        E = eAST;
        AA = aaAST;
    }

    public <TArg, TResult> TResult visit(ArrayAggregateVisitor<TArg, TResult> v, TArg arg) {
        return v.visitMultipleArrayAggregate(this, arg);
    }

}
