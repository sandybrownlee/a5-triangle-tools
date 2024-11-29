package triangle.codegen;

import triangle.repr.Expression;
import triangle.repr.RewriteStage;
import triangle.repr.Statement;

// needs to run after constant folding
class DeadCodeEliminator implements RewriteStage {

    Statement eliminateDeadCode(Statement program) {
        return rewrite(program);
    }

    @Override public Statement rewrite(final Statement statement) {
        Statement eliminated = RewriteStage.super.rewrite(statement);

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

    @Override public Expression rewrite(final Expression expression) {
        Expression eliminated = RewriteStage.super.rewrite(expression);

        if (eliminated instanceof Expression.IfExpression ifExpression &&
            ifExpression.condition() instanceof Expression.LitBool litBool) {
            return litBool.value() ? ifExpression.consequent() : ifExpression.alternative();
        }

        return eliminated;
    }

}
