package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;

public class StatisticsVisitor extends ConstantFolder { 
	
	private int characterExpressionCount = 0;
	private int integerExpressionCount = 0;
	
	public int getCharacterExpressionCount() {
		return characterExpressionCount;
	}
	
	public int getIntegerExpressionCount() {
		return integerExpressionCount;
	}
	
	public void printStatistics() {
		System.out.println("CharacterExpressions: " + characterExpressionCount);
		System.out.println("IntegerExpressions: " + integerExpressionCount);
	}
	
	@Override
	public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
		characterExpressionCount++;
		ast.CL.visit(this);
		return null;
	}
	
	@Override
	public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
		integerExpressionCount++;
		return ast;
	}
	
}