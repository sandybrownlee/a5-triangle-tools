package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {
    public final Command C1;
    public final Expression E;
    public final Command C2;

    public LoopWhileCommand(Command C1, Expression E, Command C2, SourcePosition position) {
        super(position);
        this.C1 = C1;
        this.E = E;
        this.C2 = C2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitLoopWhileCommand(this, arg);
    }
}
