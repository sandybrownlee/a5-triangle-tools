/*
 * @(#)RecordTypeDenoter.java
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

import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.syntacticAnalyzer.SourcePositionOld;

@Deprecated public class RecordTypeDenoter extends TypeDenoter {

    public FieldTypeDenoter FT;

    public RecordTypeDenoter(FieldTypeDenoter ftAST, SourcePositionOld position) {
        super(position);
        FT = ftAST;
    }

    // TODO: this equals() method is wrong, ErrorTypeDenoter is neither a super nor sub class of this class.
    //      see also: ErrorTypeDenoter.java:32
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ErrorTypeDenoter) {
            return true;
        } else if (obj instanceof RecordTypeDenoter) {
            return this.FT.equals(((RecordTypeDenoter) obj).FT);
        } else {
            return false;
        }
    }

    public <TArg, TResult> TResult visit(TypeDenoterVisitor<TArg, TResult> v, TArg arg) {
        return v.visitRecordTypeDenoter(this, arg);
    }

    @Override
    public int getSize() {
        return FT.getSize();
    }

}
