package triangle.repr;

import java.util.List;
import java.util.Optional;

sealed public interface Statement extends SourceLocatable
        permits Statement.AssignStatement, Statement.ExpressionStatement, Statement.IfStatement, Statement.LetStatement,
                Statement.LoopWhileStatement, Statement.RepeatUntilStatement, Statement.RepeatWhileStatement,
                Statement.StatementBlock, Statement.WhileStatement {

    final class StatementBlock implements Statement {

        private final List<Statement> statements;
        private       SourcePosition  sourcePos;

        public StatementBlock(List<Statement> statements) {
            this.statements = statements;
        }

        @Override public String toString() {
            return "StatementBlock[" + "sourcePos=" + sourcePos + ", " + "statements=" + statements + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public List<Statement> statements() {
            return statements;
        }

    }

    final class LetStatement implements Statement {

        private final List<Declaration> declarations;
        private final Statement         statement;
        private       SourcePosition    sourcePos;

        public LetStatement(List<Declaration> declarations, Statement statement) {
            this.declarations = declarations;
            this.statement = statement;
        }


        @Override public String toString() {
            return "LetStatement[" + "sourcePos=" + sourcePos + ", " + "declarations=" + declarations + ", " + "statement=" +
                   statement + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public List<Declaration> declarations() {
            return declarations;
        }

        public Statement statement() {
            return statement;
        }

    }

    final class IfStatement implements Statement {

        private final Expression          condition;
        private final Optional<Statement> consequent;
        private final Optional<Statement> alternative;
        private       SourcePosition      sourcePos;

        public IfStatement(
                Expression condition, Optional<Statement> consequent, Optional<Statement> alternative
        ) {
            this.condition = condition;
            this.consequent = consequent;
            this.alternative = alternative;
        }

        @Override public String toString() {
            return "IfStatement[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "consequent=" +
                   consequent + ", " + "alternative=" + alternative + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Optional<Statement> consequent() {
            return consequent;
        }

        public Optional<Statement> alternative() {
            return alternative;
        }

    }

    final class WhileStatement implements Statement {

        private final Expression     condition;
        private final Statement      body;
        private       SourcePosition sourcePos;

        public WhileStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }


        @Override public String toString() {
            return "WhileStatement[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "body=" + body + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

    }

    final class LoopWhileStatement implements Statement {

        private final Expression     condition;
        private final Statement      loopBody;
        private final Statement      doBody;
        private       SourcePosition sourcePos;

        public LoopWhileStatement(Expression condition, Statement loopBody, Statement doBody) {
            this.condition = condition;
            this.loopBody = loopBody;
            this.doBody = doBody;
        }

        @Override public String toString() {
            return "LoopWhileStatement[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "loopBody=" +
                   loopBody + ", " + "doBody=" + doBody + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement loopBody() {
            return loopBody;
        }

        public Statement doBody() {
            return doBody;
        }

    }

    final class RepeatWhileStatement implements Statement {

        private final Expression     condition;
        private final Statement      body;
        private       SourcePosition sourcePos;

        public RepeatWhileStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override public String toString() {
            return "RepeatWhileStatement[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "body=" + body +
                   ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

    }

    final class RepeatUntilStatement implements Statement {

        private final Expression     condition;
        private final Statement      body;
        private       SourcePosition sourcePos;

        public RepeatUntilStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }


        @Override public String toString() {
            return "RepeatUntilStatement[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "body=" + body +
                   ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

    }

    final class AssignStatement implements Statement {

        private final Expression.Identifier identifier;
        private final Expression            expression;
        private       SourcePosition        sourcePos;

        public AssignStatement(Expression.Identifier identifier, Expression expression) {
            this.identifier = identifier;
            this.expression = expression;
        }


        @Override public String toString() {
            return "AssignStatement[" + "sourcePos=" + sourcePos + ", " + "identifier=" + identifier + ", " + "expression=" +
                   expression + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression.Identifier identifier() {
            return identifier;
        }

        public Expression expression() {
            return expression;
        }

    }

    // to evaluate an expression just for its side-effects; note that we cannot make Expression extend Statement since
    // Expressions have to be treated differently during code-generation
    final class ExpressionStatement implements Statement {

        private final Expression     expression;
        private       SourcePosition sourcePos;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Override public String toString() {
            return "ExpressionStatement[" + "sourcePos=" + sourcePos + ", " + "expression=" + expression + ']';
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public Expression expression() {
            return expression;
        }

    }

}
