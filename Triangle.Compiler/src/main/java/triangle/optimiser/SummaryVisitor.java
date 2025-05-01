package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;

/**
 * A visitor that walks the AST and counts how many
 * BinaryExpressions, IfCommands, and WhileCommands it encounters.
 */
public class SummaryVisitor extends ConstantFolder implements ProgramVisitor<Void, AbstractSyntaxTree> {

    private int binaryCount = 0;
    private int ifCount = 0;
    private int whileCount = 0;

    /**
     * Resets all counts to zero before a new run.
     */
    public void reset() {
        binaryCount = 0;
        ifCount = 0;
        whileCount = 0;
    }

    /**
     * Returns the number of BinaryExpression nodes seen.
     */
    public int getBinaryCount() {
        return binaryCount;
    }

    /**
     * Returns the number of IfCommand nodes seen.
     */
    public int getIfCount() {
        return ifCount;
    }

    /**
     * Returns the number of WhileCommand nodes seen.
     */
    public int getWhileCount() {
        return whileCount;
    }

    /**
     * Entry point: reset counts, then visit the program's top-level command.
     */
    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        reset();
        // Visit the root command of the program
        ast.C.visit(this);
        return null;
    }

    /**
     * Count this BinaryExpression, then recurse into its children.
     */
    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryCount++;
        ast.E1.visit(this);
        ast.O.visit(this);
        ast.E2.visit(this);
        return null;
    }

    /**
     * Count this IfCommand, then recurse into its test and branches.
     */
    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        ifCount++;
        ast.E.visit(this);
        ast.C1.visit(this);
        ast.C2.visit(this);
        return null;
    }

    /**
     * Count this WhileCommand, then recurse into its test and body.
     */
    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        whileCount++;
        ast.E.visit(this);
        ast.C.visit(this);
        return null;
    }
}
