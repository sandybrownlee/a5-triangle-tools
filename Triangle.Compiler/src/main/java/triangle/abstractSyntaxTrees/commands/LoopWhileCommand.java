package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {


    public final Command c1;
    public Expression e;
    public final Command c2;

    public LoopWhileCommand(SourcePosition position, Command c1, Expression e, Command c2) {
        super(position);
        this.c1 = c1;
        this.e = e;
        this.c2 = c2;

    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg tArg) {
        return visitor.visitLoopWhileCommand(this, tArg);
    }
}
