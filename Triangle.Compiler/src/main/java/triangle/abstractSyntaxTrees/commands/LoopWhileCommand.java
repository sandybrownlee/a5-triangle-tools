package triangle.abstractSyntaxTrees.commands; // or the appropriate package

import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {
    public Command C1;
    public Expression E;
    public Command C2;

    public LoopWhileCommand(Command c1, Expression e, Command c2, SourcePosition pos) {
        super(pos);
        C1 = c1;
        E = e;
        C2 = c2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }
}
