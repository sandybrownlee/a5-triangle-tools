package triangle.optimiser;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;

public class SummaryVisitor extends ConstantFolder {

    private int ifCommandCount = 0;
	private int binaryExpressionCount = 0;
    private int whileCommandCount = 0;

    public void printStats() {
    	System.out.println("\n--- AST Node Summary ---");
        System.out.println("While:" + whileCommandCount);
        System.out.println("If: " + ifCommandCount);
        System.out.println("BinaryExp: " + binaryExpressionCount);
    }

    //visiting binaryexpression node
    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++;
        return super.visitBinaryExpression(ast, arg); //continue with actual implementation
    }

    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++;
        return super.visitIfCommand(ast, arg);
    }

    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCount++;
        return super.visitWhileCommand(ast, arg);
    }
}

