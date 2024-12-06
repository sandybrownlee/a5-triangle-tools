package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.SquareCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;

public class SummaryVisitor implements CommandVisitor<Void, Void>, ExpressionVisitor<Void, Void>, ProgramVisitor<Void, Void> {

    // Counters for different node types
    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    // Method to get the summary statistics
    public void getSummary() {
        System.out.println("Summary of the Triangle Program:");
        System.out.println("Binary Expressions: " + binaryExpressionCount);
        System.out.println("If Commands: " + ifCommandCount);
        System.out.println("While Commands: " + whileCommandCount);
    }

    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this, arg); // Visit the command in the program
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++; // Increment the count for IfCommands
        ast.E.visit(this, arg); // Visit the expression
        ast.C1.visit(this, arg); // Visit the first command
        ast.C2.visit(this, arg); // Visit the second command
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCount++; // Increment the count for WhileCommands
        ast.E.visit(this, arg); // Visit the expression
        ast.C.visit(this, arg); // Visit the command
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++; // Increment the count for BinaryExpressions
        ast.E1.visit(this, arg); // Visit the first expression
        ast.E2.visit(this, arg); // Visit the second expression
        return null;
    }

    // Implement other visit methods as needed, but they can be empty
    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfExpression'");
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLetExpression'");
    }

    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignCommand'");
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallCommand'");
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitEmptyCommand'");
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLetCommand'");
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSequentialCommand'");
    }

    @Override
    public Void visitSquareCommand(SquareCommand ast, Void arg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSquareCommand'");
    }
}