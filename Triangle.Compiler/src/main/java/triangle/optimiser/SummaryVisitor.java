package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;


 // The SummaryVisitor class extends the ConstantFolder functionality 
 
public class SummaryVisitor extends ConstantFolder {

    // Counters to keep track of occurrences of specific constructs in the AST.
	private int binaryExprCount = 0;
    private int ifCmdCount = 0;
    private int whileCmdCount = 0;

	public void countStats(Program ast) {
        // The visit method traverses the entire AST, delegating work to overridden visit methods.
		ast.visit(this);
		
        // Output collected statistics to the console.
		System.out.println("Number of Binary Expressions: " + binaryExprCount);
        System.out.println("Number of If Commands: " + ifCmdCount);
        System.out.println("Number of While Commands: " + whileCmdCount);
    }

    
    // Generates a summary of the collected statistics as a formatted string.
     
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Number of Binary Expressions: ").append(binaryExprCount).append("\n");
        summary.append("Number of If Commands: ").append(ifCmdCount).append("\n");
        summary.append("Number of While Commands: ").append(whileCmdCount).append("\n");
        return summary.toString();
    }

    // Overridden visit methods to process specific nodes in the AST.
	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        // Visit the left and right subexpressions recursively.
		AbstractSyntaxTree leftReplacement = ast.E1.visit(this);
        AbstractSyntaxTree rightReplacement = ast.E2.visit(this);

        // Increment the binary expression count.
		binaryExprCount++;

        // Visit the operator as well (in case it contains metadata to be processed).
		ast.O.visit(this);

        // Attempt to perform constant folding if both subexpressions can be replaced.
		if (leftReplacement != null && rightReplacement != null) {
            // Folding binary expressions using a method inherited from ConstantFolder.
            return foldBinaryExpression(leftReplacement, rightReplacement, ast.O);
        } else if (leftReplacement != null) {
            // Replace only the left subexpression if it can be optimized.
            ast.E1 = (Expression) leftReplacement;
        } else if (rightReplacement != null) {
            // Replace only the right subexpression if it can be optimized.
            ast.E2 = (Expression) rightReplacement;
        }

        // Return null to indicate no top-level replacement.
        return null;
	}

    
	@Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        // Visit the commands associated with the "then" and "else" branches.
        ast.C1.visit(this);
        ast.C2.visit(this);

        // Increment the count for if commands.
        ifCmdCount++;

        // Attempt to optimize the condition expression by visiting it.
        AbstractSyntaxTree conditionReplacement = ast.E.visit(this);
        if (conditionReplacement != null) {
            // Replace the condition with the optimized version if available.
            ast.E = (Expression) conditionReplacement;
        }
        return null;
    }

    
	@Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        // Visit the body of the while loop.
        ast.C.visit(this);

        // Increment the count for while commands.
        whileCmdCount++;

        // Attempt to optimize the loop's condition expression by visiting it.
        AbstractSyntaxTree conditionReplacement = ast.E.visit(this);
        if (conditionReplacement != null) {
            // Replace the condition with the optimized version if available.
            ast.E = (Expression) conditionReplacement;
        }
        return null;
    }

}
