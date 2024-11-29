package triangle.repr;

import triangle.util.ASTVisitor;

import java.util.List;
import java.util.Optional;

sealed public abstract class Statement implements Annotatable.SourceLocatable {

    protected SourcePosition sourcePos;

    @Override public void setSourcePosition(final SourcePosition sourcePos) {
        this.sourcePos = sourcePos;
    }

    @Override public SourcePosition sourcePosition() {
        return sourcePos;
    }

    public abstract void visit(ASTVisitor visitor);

    public static final class StatementBlock extends Statement {

        private final List<Statement> statements;

        public StatementBlock(List<Statement> statements) {
            this.statements = statements;
        }

        public List<Statement> statements() {
            return statements;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitStatementBlock(this);
        }
    }

    public static final class LetStatement extends Statement {

        private final List<Declaration> declarations;
        private final Statement         statement;

        public LetStatement(List<Declaration> declarations, Statement statement) {
            this.declarations = declarations;
            this.statement = statement;
        }

        public List<Declaration> declarations() {
            return declarations;
        }

        public Statement statement() {
            return statement;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitLetStatement(this);
        }
    }

    public static final class IfStatement extends Statement {

        private final Expression          condition;
        private final Optional<Statement> consequent;
        private final Optional<Statement> alternative;

        public IfStatement(
                Expression condition, Optional<Statement> consequent, Optional<Statement> alternative
        ) {
            this.condition = condition;
            this.consequent = consequent;
            this.alternative = alternative;
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

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitIfStatement(this);
        }
    }

    public static final class WhileStatement extends Statement {

        private final Expression condition;
        private final Statement  body;

        public WhileStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitWhileStatement(this);
        }
    }

    public static final class LoopWhileStatement extends Statement {

        private final Expression condition;
        private final Statement  loopBody;
        private final Statement  doBody;

        public LoopWhileStatement(Expression condition, Statement loopBody, Statement doBody) {
            this.condition = condition;
            this.loopBody = loopBody;
            this.doBody = doBody;
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

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitLoopWhileStatement(this);
        }
    }

    public static final class RepeatWhileStatement extends Statement {

        private final Expression condition;
        private final Statement  body;

        public RepeatWhileStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitRepeatWhileStatement(this);
        }
    }

    public static final class RepeatUntilStatement extends Statement {

        private final Expression condition;
        private final Statement  body;

        public RepeatUntilStatement(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        public Expression condition() {
            return condition;
        }

        public Statement body() {
            return body;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitRepeatUntilStatement(this);
        }
    }

    public static final class AssignStatement extends Statement {

        private final Expression.Identifier identifier;
        private final Expression            expression;

        public AssignStatement(Expression.Identifier identifier, Expression expression) {
            this.identifier = identifier;
            this.expression = expression;
        }

        public Expression.Identifier identifier() {
            return identifier;
        }

        public Expression expression() {
            return expression;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitAssignStatement(this);
        }
    }

    public static final class ExpressionStatement extends Statement {

        private final Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        public Expression expression() {
            return expression;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitExpressionStatement(this);
        }
    }

    public static final class NoopStatement extends Statement {

        public static final NoopStatement NOOP_STATEMENT = new NoopStatement();

        private NoopStatement() { }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitNoopStatement(this);
        }
    }

}
