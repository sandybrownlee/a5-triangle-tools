package triangle.ast;

import java.util.List;
import java.util.Optional;

sealed public interface Statement extends AST
        permits Expression, Statement.AssignStatement, Statement.BeginStatement, Statement.CallStatement, Statement.IfStatement,
                Statement.LetStatement, Statement.WhileStatement {

    record BeginStatement(List<Statement> statements) implements Statement { }

    record LetStatement(List<Declaration> declarations, Statement statement) implements Statement { }

    record IfStatement(Expression condition, Optional<Statement> consequent, Optional<Statement> alternative)
            implements Statement { }

    record WhileStatement(Expression condition, Statement body) implements Statement { }

    record CallStatement(Expression.Identifier callable, List<Argument> arguments) implements Statement { }

    record AssignStatement(Expression.Identifier lvalue, Expression expression) implements Statement { }

}
