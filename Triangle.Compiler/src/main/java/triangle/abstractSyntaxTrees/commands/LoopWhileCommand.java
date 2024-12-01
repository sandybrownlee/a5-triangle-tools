package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {

    public LoopWhileCommand(Expression eAST, Command preCommandAST, Command postCommandAST, SourcePosition position) {
        super(position);
        E = eAST;
        PreCommand = preCommandAST;
        PostCommand = postCommandAST;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }

    public Expression E;
    public final Command PreCommand;
    public final Command PostCommand;
}
