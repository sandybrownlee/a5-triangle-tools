package triangle.visitors;

import triangle.abstractSyntaxTrees.*;

public class SummaryVisitor implements Visitor {

    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    public int getBinaryExpressionCount() { return binaryExpressionCount; }
    public int getIfCommandCount() { return ifCommandCount; }
    public int getWhileCommandCount() { return whileCommandCount; }

    @Override
    public void visitBinaryExpression(BinaryExpression expr) { binaryExpressionCount++; }
    @Override
    public void visitIfCommand(IfCommand cmd) { ifCommandCount++; }
    @Override
    public void visitWhileCommand(WhileCommand cmd) { whileCommandCount++; }
}
