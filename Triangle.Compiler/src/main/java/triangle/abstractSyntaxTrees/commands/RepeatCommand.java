<<<<<<< HEAD
=======
/*
 * @(#)WhileCommand.java                       
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

>>>>>>> f7b86a0427edcc2f17d808482e39fc93ef35de73
package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class RepeatCommand extends Command {

	public RepeatCommand(Expression eAST, Command cAST, SourcePosition position) {
		super(position);
		E = eAST;
		C = cAST;
	}

	public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
		return v.visitRepeatCommand(this, arg);
	}

	public Expression E;
	public final Command C;
}
