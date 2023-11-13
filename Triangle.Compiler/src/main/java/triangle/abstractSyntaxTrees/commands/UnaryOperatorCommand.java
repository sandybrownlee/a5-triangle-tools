package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.syntacticAnalyzer.SourcePosition;

/**
 * This class handles unary operators such as ++, -- and **
 */
public class UnaryOperatorCommand extends Command {
    /**
     * Constructor
     * @param AST the AST to output to
     * @param o the operator to process
     * @param factor the factor to which the binary operatio will increment or decrement
     * @param position the source position
     */
    public UnaryOperatorCommand(Vname AST, Operator o, int factor, SourcePosition position) {
        super(position);
        V = AST;
        System.out.println(o.spelling);

        // construct the correct binary expression with the correct factor to increment or decrement by
        E = new BinaryExpression(
                new VnameExpression(AST, position), o, new IntegerExpression(new IntegerLiteral(String.valueOf(factor), position),
                position),
            position);
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg tArg) {
        return visitor.visitUnaryOperatorCommand(this, tArg);
    }

    public final Vname V;
    public Expression E;
}
