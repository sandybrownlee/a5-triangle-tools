/*
 * @(#)ArrayTypeDenoter.java
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

package triangle.abstractSyntaxTrees.types;

import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.syntacticAnalyzer.SourcePositionOld;

@Deprecated public class ArrayTypeDenoter extends TypeDenoter {

    public final IntegerLiteral IL;
    public       TypeDenoter    T;

    public ArrayTypeDenoter(IntegerLiteral ilAST, TypeDenoter tAST, SourcePositionOld position) {
        super(position);
        IL = ilAST;
        T = tAST;
    }

    // TODO: this equals() method is wrong, ErrorTypeDenoter is neither a super nor sub class of this class.
    //      see also: ErrorTypeDenoter.java:32
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ErrorTypeDenoter) {
            return true;
        } else if (obj instanceof ArrayTypeDenoter) {
            return this.IL.spelling.compareTo(((ArrayTypeDenoter) obj).IL.spelling) == 0
                   && this.T.equals(((ArrayTypeDenoter) obj).T);
        } else {
            return false;
        }
    }

    public <TArg, TResult> TResult visit(TypeDenoterVisitor<TArg, TResult> v, TArg arg) {
        return v.visitArrayTypeDenoter(this, arg);
    }

    @Override
    public int getSize() {
        return IL.getValue() * T.getSize();
    }

}
