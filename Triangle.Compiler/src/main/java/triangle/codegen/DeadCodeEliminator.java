package triangle.codegen;

import triangle.repr.Expression;
import triangle.repr.Visitor;
import triangle.repr.Statement;

// needs to run after constant folding
class DeadCodeEliminator implements Visitor {

    Statement eliminateDeadCode(Statement program) {
        return visit(program);
    }

    @Override public Statement visit(final Statement statement) {
        Statement eliminated = Visitor.super.visit(statement);

        return switch (eliminated) {
            case Statement.IfStatement ifS when ifS.condition() instanceof Expression.LitBool litBool ->
                    litBool.value() ? ifS.consequent().orElse(Statement.NoopStatement.NOOP_STATEMENT) : ifS.alternative().orElse(
                            Statement.NoopStatement.NOOP_STATEMENT);
            case Statement.RepeatUntilStatement ruS when ruS.condition() instanceof Expression.LitBool litBool ->
                    litBool.value() ? ruS : Statement.NoopStatement.NOOP_STATEMENT;
            case Statement.RepeatWhileStatement rwS when rwS.condition() instanceof Expression.LitBool litBool ->
                    litBool.value() ? rwS : Statement.NoopStatement.NOOP_STATEMENT;
            case Statement.WhileStatement wS when wS.condition() instanceof Expression.LitBool litBool ->
                    litBool.value() ? wS : Statement.NoopStatement.NOOP_STATEMENT;
            default -> eliminated;
        };
    }

    @Override public Expression visit(final Expression expression) {
        Expression eliminated = Visitor.super.visit(expression);

        if (eliminated instanceof Expression.IfExpression ifExpression &&
            ifExpression.condition() instanceof Expression.LitBool litBool) {
            return litBool.value() ? ifExpression.consequent() : ifExpression.alternative();
        }

        return eliminated;
    }

}
