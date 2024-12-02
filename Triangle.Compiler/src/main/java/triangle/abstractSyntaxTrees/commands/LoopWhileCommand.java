package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {

    public final Command C1; // Command before the condition
    public Expression E; // Loop condition
    public final Command C2; // Command after the condition

    public LoopWhileCommand(Command c1AST, Expression eAST, Command c2AST, SourcePosition position) {
        super(position);
        this.C1 = c1AST;
        this.E = eAST;
        this.C2 = c2AST;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }
}
