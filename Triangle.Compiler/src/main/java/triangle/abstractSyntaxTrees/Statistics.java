package triangle.abstractSyntaxTrees;

import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.visitors.StatisticsVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class Statistics extends AbstractSyntaxTree {
	public Statistics(Command cAST, SourcePosition position) {
		super(position);
		C = cAST;
	}
	
	public Command C;
	
//    public <TArg, TResult> TResult visit(StatisticsVisitor<TArg, TResult> visitor, TArg arg) {
//        return visitor.visitStatistics(this, arg);
//    }

//    public <TResult> TResult visit(StatisticsVisitor<Void, TResult> visitor) {
//        return visit(visitor, null);
//    }

	private int charCount = 0;
    private int intCount = 0;

    public void countChar(CharacterExpression ast) {
        charCount++;
    }

    public void countInt(IntegerExpression ast) {
        intCount++;
    }

    public void printCounts() {
        System.out.println("Character Expressions: " + charCount);
        System.out.println("Integer Expressions: " + intCount);
    }
}
