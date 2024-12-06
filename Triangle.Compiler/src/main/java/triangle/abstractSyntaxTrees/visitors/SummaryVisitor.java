package triangle.abstractSyntaxTrees.visitors;

import triangle.ast.*;
import triangle.ast.expression.BinaryExpression;
import triangle.ast.command.IfCommand;
import triangle.ast.command.WhileCommand;

public class SummaryVisitor implements Visitor {

	// declaring integers to count the types of AST nodes
	private int binaryExpressionsCount = 0;
	private int ifCommandsCount = 0;
	private int whileCommandsCount = 0;
	
	// visits a binary expression AST node and increments the count
	@Override
	public void visitBinaryExpression(BinaryExpression ast) {
		binaryExpressionsCount++;
		ast.left.visit(this);
		ast.right.visit(this);
	}
	
	// visits a if command AST node and increments the count
	@Override
    public void visitIfCommand(IfCommand ast) {
        ifCommandsCount++;
        ast.expression.visit(this);
        ast.thenCommand.visit(this);
        // vists the else command too
        if (ast.elseCommand != null) {
            ast.elseCommand.visit(this);
        }
    }

	// visits a while command AST node and increments the count
    @Override
    public void visitWhileCommand(WhileCommand ast) {
        whileCommandsCount++;
        ast.expression.visit(this);
        ast.command.visit(this);
    }
    
    // formats the counts in a single string to be printed
    public String getSummary() {
        return String.format("Binary Expressions: " + binaryExpressionsCount + "\n If Commands: " + ifCommandsCount + "\nWhile Commands: " +
                             whilesCommandCount);
    }
}
