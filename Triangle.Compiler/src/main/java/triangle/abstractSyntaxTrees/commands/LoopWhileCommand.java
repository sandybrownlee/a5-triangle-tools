
package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {

    public Command C1;
    public Expression E;
    public Command C2;

    public LoopWhileCommand(Expression eAST, Command cAST, Command cAST2, SourcePosition position) {
        super(position);
        C1 = cAST;
        E = eAST;
        C2 = cAST2;
    }
    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }
}
