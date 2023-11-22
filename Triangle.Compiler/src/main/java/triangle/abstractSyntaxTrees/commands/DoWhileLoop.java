package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class DoWhileLoop extends Command {

    private final Expression E;
	private final Command C1;
	private final Command C2;

    public DoWhileLoop(Expression eAST,Command c1AST, Command c2AST, SourcePosition position) {
        super(position);
        E = eAST;
		C1 = c1AST;
		C2 = c2AST;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitDoWhileLoop(this, arg);
    }
    
}
