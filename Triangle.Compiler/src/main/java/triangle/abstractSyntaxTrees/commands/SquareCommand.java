package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class SquareCommand extends Command {

    public final Expression base; // The base expression to be squared

    public SquareCommand(Expression base, SourcePosition position) {
        super(position);
        this.base = base; // Initialize the base expression
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitSquareCommand(this, arg); // Visit method for the SquareCommand
    }
}