package triangle.optimiser;

import triangle.Compiler;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.expressions.*;

public class SummaryVisitor extends ConstantFolder {

	private int bExpressionCount = 0;
	private int ifExpressionCount = 0;
	private int whileExpressionCount = 0;

	public void countStats(Program ast) {
		ast.visit(this);
		System.out.println("----------------------Summary Statistics----------------------");
		System.out.println("Number of Binary Expressions: " + bExpressionCount);
		System.out.println("Number of If Expressions: " + ifExpressionCount);
		System.out.println("Number of While Expressions: " + whileExpressionCount);
		System.out.println("--------------------------------------------------------------");
	}

	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement1 = ast.E1.visit(this);
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		bExpressionCount++; // count before each visit to increment properly.
		ast.O.visit(this);
		// if visiting a child node returns something, it's either the original constant
		// (IntegerLiteral) or a folded version replacing the expression at that child
		// node
		// If both child nodes are not null; return a folded version of this
		// BinaryExpression
		// Otherwise, at least one child node isn't constant (foldable) so just replace
		// the
		// foldable child nodes with their folded equivalent and return null

		if (Compiler.folding) {
			if (replacement1 != null && replacement2 != null) {
				return foldBinaryExpression(replacement1, replacement2, ast.O);
			} else if (replacement1 != null) {
				ast.E1 = (Expression) replacement1;
			} else if (replacement2 != null) {
				ast.E2 = (Expression) replacement2;
			}
		}
		// if we get here, we can't fold any higher than this level
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
		ast.C1.visit(this);
		ast.C2.visit(this);
		ifExpressionCount++; // count before replacement to increment actual values properly.
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
		ast.C.visit(this);
		whileExpressionCount++;
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

}
