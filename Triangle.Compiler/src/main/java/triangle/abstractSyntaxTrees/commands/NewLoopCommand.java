package triangle.abstractSyntaxTrees.commands;


import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class NewLoopCommand extends Command {
    public NewLoopCommand(Expression eAST, Command c1AST, Command c2AST, SourcePosition position){
        super(position);

        C1 = c1AST;
        E = eAST;
        C2 = c2AST;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg){
        return v.visitNewLoopCommand(this, arg);

    }

    public Expression E;
    public final Command C1, C2;
}
