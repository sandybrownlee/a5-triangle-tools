package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

/**
 * Represents the 'loop C1 while E do C2' construct.
 */
public class WeirdWhileCommand extends Command {

    public final Command C1; // Initial command to execute
    public final Expression E; // Expression to evaluate
    public final Command C2; // Command to execute when condition is true

    public WeirdWhileCommand(Command c1, Expression e, Command c2, SourcePosition position) {
        super(position);
        this.C1 = c1;
        this.E = e;
        this.C2 = c2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitWeirdWhileCommand(this, arg);
    }
}
