package triangle.repr;

import java.util.List;
import java.util.Optional;

sealed public interface Statement
        permits Statement.AssignStatement, Statement.ExpressionStatement, Statement.IfStatement, Statement.LetStatement,
                Statement.LoopWhileStatement, Statement.RepeatUntilStatement, Statement.RepeatWhileStatement,
                Statement.StatementBlock, Statement.WhileStatement {

    record StatementBlock(SourcePosition sourcePos, List<Statement> statements) implements Statement { }

    record LetStatement(SourcePosition sourcePos, List<Declaration> declarations, Statement statement) implements Statement { }

    record IfStatement(SourcePosition sourcePos, Expression condition, Optional<Statement> consequent,
                       Optional<Statement> alternative) implements Statement { }

    record WhileStatement(SourcePosition sourcePos, Expression condition, Statement body) implements Statement { }

    record LoopWhileStatement(SourcePosition sourcePos, Expression condition, Statement loopBody, Statement doBody)
            implements Statement { }

    record RepeatWhileStatement(SourcePosition sourcePos, Expression condition, Statement body) implements Statement { }

    record RepeatUntilStatement(SourcePosition sourcePos, Expression condition, Statement body) implements Statement { }

    record AssignStatement(SourcePosition sourcePos, Expression.Identifier identifier, Expression expression)
            implements Statement { }

    // to evaluate an expression just for its side-effects; note that we cannot make Expression extend Statement since
    // Expressions have to be treated differently during code-generation
    record ExpressionStatement(SourcePosition sourcePos, Expression expression) implements Statement { }

}
