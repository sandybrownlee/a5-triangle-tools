/*
 * @(#)ConstDeclaration.java                       
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

package triangle.abstractSyntaxTrees.declarations;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.types.TypeDenoter;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class ConstDeclaration extends Declaration implements ConstantDeclaration {

	public ConstDeclaration(Identifier iAST, Expression eAST, SourcePosition position) {
		super(position);
		I = iAST;
		E = eAST;
	}
	public ConstDeclaration(Identifier iAST, TypeDenoter typeAST, SourcePosition position) {
        super(position);
        I = iAST;
        E = null; // No expression provided in this case
        this.type = typeAST;
    }

	@Override
    public TypeDenoter getType() {
        return (E != null) ? E.type : type; // Return type from the expression or the directly assigned type
    }

    public <TArg, TResult> TResult visit(DeclarationVisitor<TArg, TResult> v, TArg arg) {
        return v.visitConstDeclaration(this, arg);
    }

    public final Identifier I;
    public Expression E; // Expression for the constant (optional)
    public TypeDenoter type; // Direct type assignment for constants like false, true
}
