package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;
 
public class LoopCommand extends Command {
    public final Command C1;
    public final Expression E;
    public final Command C2;

    public LoopCommand(Command c1, Expression e, Command c2, SourcePosition position) {
        super(position);
        this.C1 = c1;
        this.E = e;
        this.C2 = c2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitLoopCommand(this, arg);
    }
}