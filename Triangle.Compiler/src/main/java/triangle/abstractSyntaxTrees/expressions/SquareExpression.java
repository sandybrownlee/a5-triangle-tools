package triangle.abstractSyntaxTrees.expressions;

import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class SquareExpression extends Expression {
    public Expression expression;

    public SquareExpression(Expression expr, SourcePosition position) {
        super(position);
        this.expression = expr;
    }

    @Override
    public <TArg, TResult> TResult visit(ExpressionVisitor<TArg, TResult> v, TArg arg) {
        return v.visitSquareExpression(this, arg);
    }
}

