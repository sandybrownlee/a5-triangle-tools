package triangle.abstractSyntaxTrees.commands;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;


public class loopWhileCommand extends Command {

    public loopWhileCommand(Command c1AST, Expression eAST, Command c2AST, SourcePosition position) {
        super(position);
        this.c1AST = c1AST;
        this.eAST = eAST;
        this.c2AST = c2AST;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }

    public Expression eAST;
    public final Command c1AST, c2AST;
}