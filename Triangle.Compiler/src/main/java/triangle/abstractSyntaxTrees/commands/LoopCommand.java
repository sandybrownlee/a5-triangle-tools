package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopCommand extends AbstractSyntaxTree {
    
    public Command C1;  // First command (C1)
    public Expression E;  // Condition (E)
    public Command C2;  // Second command (C2)

    public LoopCommand(Command c1, Expression e, Command c2, SourcePosition position) {
        super(position);  // Call the parent constructor
        C1 = c1;
        E = e;
        C2 = c2;
    }

    // The visitor pattern will be used to traverse and process this AST node
    public Object visit(Visitor v, Object o) {
        return v.visitLoopCommand(this, o);
    }
}
