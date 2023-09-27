package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.syntacticAnalyzer.SourcePosition;

public class IncrementDecrementCommand extends Command {
    public IncrementDecrementCommand(Vname AST, Operator o, SourcePosition position) {
        super(position);
        V = AST;
        System.out.println(o.spelling);
        E = new BinaryExpression(
                new VnameExpression(AST, position), o, new IntegerExpression(new IntegerLiteral("1", position),
                position),
            position);
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg tArg) {
        return visitor.visitIncrementDecrementCommand(this, tArg);
    }

    public final Vname V;
    public Expression E;
}
