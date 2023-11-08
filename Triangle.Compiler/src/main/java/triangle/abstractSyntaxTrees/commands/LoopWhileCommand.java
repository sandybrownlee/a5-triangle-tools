package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command{
    public LoopWhileCommand(Expression eAST, Command cAST1,Command cAST2, SourcePosition position) {
        super(position);
        E = eAST;
        C1 = cAST1;
        C2 = cAST2;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }

    public Expression E;
    public final Command C1;
    public final Command C2;
}

