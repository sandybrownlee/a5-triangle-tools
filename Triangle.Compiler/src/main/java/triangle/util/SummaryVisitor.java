package triangle.util;

import triangle.repr.Expression;
import triangle.repr.RewriteStage;
import triangle.repr.Statement;

public class SummaryVisitor implements RewriteStage {

    private int whileStatements = 0;
    private int ifStatements = 0;
    private int binaryOps = 0;

    public Summary generateSummary(Statement program) {
        visit(program);

        Summary summary = new Summary(whileStatements, ifStatements, binaryOps);

        // reset, in case someone wants to reuse this class
        whileStatements = 0;
        ifStatements = 0;
        binaryOps = 0;

        return summary;
    }

    @Override public Statement visit(final Statement statement) {
        if (statement instanceof Statement.WhileStatement) {
            whileStatements++;
        }

        if (statement instanceof Statement.IfStatement) {
            ifStatements++;
        }

        return RewriteStage.super.visit(statement);
    }

    @Override public Expression visit(final Expression expression) {
        if (expression instanceof Expression.BinaryOp) {
            binaryOps++;
        }

        return RewriteStage.super.visit(expression);
    }

    public record Summary(int whileStatements, int ifStatements, int binaryOps) { }
}
