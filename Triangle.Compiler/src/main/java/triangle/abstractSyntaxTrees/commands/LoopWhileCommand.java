
/*
package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {
   

    public LoopWhileCommand(Command c1AST, Expression eAST, Command c2AST, SourcePosition position) {
        super(position);
        C1 = c1AST;
        E = eAST;
        C2 = c2AST;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }
    
    public final Command C1;   // first command (before the loop body)
    public final Expression E; // the loop condition expression
    public final Command C2;   // second command (inside the loop body)
}
*/
