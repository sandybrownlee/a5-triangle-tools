package triangle.optimiser;

import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.visitors.Visitor;

public class SummaryVisitor implements Visitor {

    // Counters for the statistics
    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    // Getter methods for the statistics
    public int getBinaryExpressionCount() {
        return binaryExpressionCount;
    }

    public int getIfCommandCount() {
        return ifCommandCount;
    }

    public int getWhileCommandCount() {
        return whileCommandCount;
    }

    // Reset statistics (in case of reuse)
    public void reset() {
        binaryExpressionCount = 0;
        ifCommandCount = 0;
        whileCommandCount = 0;
    }

    // Visit method for BinaryExpression nodes
    public void visit(BinaryExpression node) {
        binaryExpressionCount++;
    }

    // Visit method for IfCommand nodes
    public void visit(IfCommand node) {
        ifCommandCount++;
    }

    // Visit method for WhileCommand nodes
    public void visit(WhileCommand node) {
        whileCommandCount++;
    }

    // Starting point of the visit for Program nodes
    public void visitProgram(Program node) {
        node.getCommand().accept(this); // Visit the command of the program
    }
}
