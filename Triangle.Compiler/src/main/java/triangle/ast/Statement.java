package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

sealed public interface Statement permits Expression, Statement.AssignStatement, Statement.IfStatement, Statement.LetStatement,
                                          Statement.LoopWhileStatement, Statement.RepeatUntilStatement,
                                          Statement.RepeatWhileStatement, Statement.StatementBlock, Statement.WhileStatement {

    SourcePosition sourcePos();

    final class StatementBlock implements Statement {

        private final SourcePosition  sourcePos;
        private final List<Statement> statements;

        public StatementBlock(SourcePosition sourcePos, List<Statement> statements) {
            this.sourcePos = sourcePos;
            this.statements = statements;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Statement> statements() {
            return statements;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, statements);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (StatementBlock) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.statements, that.statements);
        }

        @Override
        public String toString() {
            return "StatementBlock[" +
                   "sourcePos=" + sourcePos + ", " +
                   "statements=" + statements + ']';
        }

    }

    final class LetStatement implements Statement {

        private final SourcePosition    sourcePos;
        private final List<Declaration> declarations;
        private final Statement         statement;

        public LetStatement(SourcePosition sourcePos, List<Declaration> declarations, Statement statement) {
            this.sourcePos = sourcePos;
            this.declarations = declarations;
            this.statement = statement;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Declaration> declarations() {
            return declarations;
        }

        public Statement statement() {
            return statement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, declarations, statement);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (LetStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.declarations, that.declarations) &&
                   Objects.equals(this.statement, that.statement);
        }

        @Override
        public String toString() {
            return "LetStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "declarations=" + declarations + ", " +
                   "statement=" + statement + ']';
        }

    }

    final class IfStatement
            implements Statement {

        private final SourcePosition      sourcePos;
        private final Expression          condition;
        private final Optional<Statement> consequent;
        private final Optional<Statement> alternative;

        public IfStatement(
                SourcePosition sourcePos, Expression condition, Optional<Statement> consequent,
                Optional<Statement> alternative
        ) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.consequent = consequent;
            this.alternative = alternative;
        }

        @Override public SourcePosition sourcePos() {
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

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, condition, consequent, alternative);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (IfStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.condition, that.condition) &&
                   Objects.equals(this.consequent, that.consequent) &&
                   Objects.equals(this.alternative, that.alternative);
        }

        @Override
        public String toString() {
            return "IfStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "condition=" + condition + ", " +
                   "consequent=" + consequent + ", " +
                   "alternative=" + alternative + ']';
        }

    }

    final class WhileStatement implements Statement {

        private final SourcePosition sourcePos;
        private final Expression     condition;
        private final Statement      body;

        public WhileStatement(SourcePosition sourcePos, Expression condition, Statement body) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.body = body;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, condition, body);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (WhileStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.condition, that.condition) &&
                   Objects.equals(this.body, that.body);
        }

        @Override
        public String toString() {
            return "WhileStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "condition=" + condition + ", " +
                   "body=" + body + ']';
        }

    }

    final class LoopWhileStatement
            implements Statement {

        private final SourcePosition sourcePos;
        private final Expression     condition;
        private final Statement      loopBody;
        private final Statement      doBody;

        public LoopWhileStatement(SourcePosition sourcePos, Expression condition, Statement loopBody, Statement doBody) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.loopBody = loopBody;
            this.doBody = doBody;
        }

        @Override public SourcePosition sourcePos() {
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

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, condition, loopBody, doBody);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (LoopWhileStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.condition, that.condition) &&
                   Objects.equals(this.loopBody, that.loopBody) &&
                   Objects.equals(this.doBody, that.doBody);
        }

        @Override
        public String toString() {
            return "LoopWhileStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "condition=" + condition + ", " +
                   "loopBody=" + loopBody + ", " +
                   "doBody=" + doBody + ']';
        }

    }

    final class RepeatWhileStatement implements Statement {

        private final SourcePosition sourcePos;
        private final Expression     condition;
        private final Statement      body;

        public RepeatWhileStatement(SourcePosition sourcePos, Expression condition, Statement body) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.body = body;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, condition, body);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (RepeatWhileStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.condition, that.condition) &&
                   Objects.equals(this.body, that.body);
        }

        @Override
        public String toString() {
            return "RepeatWhileStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "condition=" + condition + ", " +
                   "body=" + body + ']';
        }

    }

    final class RepeatUntilStatement implements Statement {

        private final SourcePosition sourcePos;
        private final Expression     condition;
        private final Statement      body;

        public RepeatUntilStatement(SourcePosition sourcePos, Expression condition, Statement body) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.body = body;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, condition, body);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (RepeatUntilStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.condition, that.condition) &&
                   Objects.equals(this.body, that.body);
        }

        @Override
        public String toString() {
            return "RepeatUntilStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "condition=" + condition + ", " +
                   "body=" + body + ']';
        }

    }

    final class AssignStatement
            implements Statement {

        private final SourcePosition        sourcePos;
        private final Expression.Identifier identifier;
        private final Expression            expression;

        public AssignStatement(SourcePosition sourcePos, Expression.Identifier identifier, Expression expression) {
            this.sourcePos = sourcePos;
            this.identifier = identifier;
            this.expression = expression;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression.Identifier identifier() {
            return identifier;
        }

        public Expression expression() {
            return expression;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourcePos, identifier, expression);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (AssignStatement) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) &&
                   Objects.equals(this.identifier, that.identifier) &&
                   Objects.equals(this.expression, that.expression);
        }

        @Override
        public String toString() {
            return "AssignStatement[" +
                   "sourcePos=" + sourcePos + ", " +
                   "identifier=" + identifier + ", " +
                   "expression=" + expression + ']';
        }

    }

}
