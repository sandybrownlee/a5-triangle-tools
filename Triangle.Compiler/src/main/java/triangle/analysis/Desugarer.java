package triangle.analysis;

import triangle.repr.Declaration;
import triangle.repr.Declaration.ConstDeclaration;
import triangle.repr.Expression;
import triangle.repr.Expression.BinaryOp;
import triangle.repr.Expression.Identifier.BasicIdentifier;
import triangle.repr.Expression.LetExpression;
import triangle.repr.Expression.LitInt;
import triangle.repr.Expression.SequenceExpression;
import triangle.repr.Expression.UnaryOp;
import triangle.repr.Visitor;
import triangle.repr.Statement;
import triangle.repr.Statement.AssignStatement;

import java.util.List;

// Desugars ++ and **
public class Desugarer implements Visitor {

    private int fresh = 0;

    @Override public Expression visit(final Expression expression) {
        Expression rewritten = Visitor.super.visit(expression);

        // if "++"
        if (rewritten instanceof UnaryOp unaryOp && unaryOp.operator().equals("++")) {
            if (unaryOp.operand() instanceof BasicIdentifier identifier) {
                // after identifier := identifier + 1 return identifier
                Expression desugared = new BinaryOp("+", identifier, new LitInt(1));
                Statement assignStatement = new AssignStatement(identifier, desugared);
                return new SequenceExpression(assignStatement, identifier).withSourcePosition(unaryOp.sourcePosition());
            }

            BasicIdentifier generated = new BasicIdentifier("rw_increment_" + fresh++);
            // let const <generated_name> ~ unaryop.operand() in <generated_name> + 1
            List<Declaration> declarations = List.of(new ConstDeclaration(generated.name(), unaryOp.operand()));
            Expression letExpression = new BinaryOp("+", generated, new LitInt(1));
            return new LetExpression(declarations, letExpression).withSourcePosition(unaryOp.sourcePosition());
        }

        // if "**"
        if (rewritten instanceof UnaryOp unaryOp && unaryOp.operator().equals("**")) {
            if (unaryOp.operand() instanceof BasicIdentifier identifier) {
                // after identifier := identifier * identifier return identifier
                Expression desugared = new BinaryOp("*", identifier, identifier);
                Statement assignStatement = new AssignStatement(identifier, desugared);
                return new SequenceExpression(assignStatement, identifier).withSourcePosition(unaryOp.sourcePosition());
            }

            BasicIdentifier generated = new BasicIdentifier("rw_square_" + fresh++);
            // must not duplicate expression in const declaration, in case it has side-effects
            List<Declaration> declarations = List.of(new ConstDeclaration(generated.name(), unaryOp.operand()));
            // let const <generated_name> ~ unaryop.operand() in <generated_name> * <generated_name>
            Expression letExpression = new BinaryOp("*", generated, generated);
            return new LetExpression(declarations, letExpression).withSourcePosition(unaryOp.sourcePosition());
        }

        return rewritten;
    }

    Statement desugar(Statement program) {
        return visit(program);
    }

}
