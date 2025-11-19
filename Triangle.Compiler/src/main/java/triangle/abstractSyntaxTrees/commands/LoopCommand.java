package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopCommand extends Command {

    public LoopCommand(Command c1AST, Expression eAST,Command c2AST, SourcePosition position) {
        super(position);
        C1 = c1AST;
        E = eAST;
        C2 = c2AST;
    }

    public <T, P> P visit(CommandVisitor<T, P> v, T o) {
        return v.visitLoopCommand(this, o);
    }

    public final Command C1;
    public final Expression E;
    public final Command C2;
}