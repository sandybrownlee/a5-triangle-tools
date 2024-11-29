package triangle.repr;

import triangle.util.ASTVisitor;

sealed public abstract class Argument implements Annotatable.Typeable, Annotatable.SourceLocatable
        permits Argument.FuncArgument, Argument.VarArgument, Expression {

    protected SourcePosition sourcePos;
    protected Type           type;

    @Override public Type getType() {
        return type;
    }

    @Override public void setType(final Type type) {
        this.type = type;
    }

    @Override public void setSourcePosition(final SourcePosition sourcePos) {
        this.sourcePos = sourcePos;
    }

    @Override public SourcePosition sourcePosition() {
        return sourcePos;
    }

    public abstract void visit(ASTVisitor visitor);

    public static final class VarArgument extends Argument {

        private final Expression.Identifier var;

        public VarArgument(Expression.Identifier var) {
            this.var = var;
        }

        public Expression.Identifier var() {
            return var;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitVarArgument(this);
        }
    }

    public static final class FuncArgument extends Argument {

        private final Expression.Identifier.BasicIdentifier func;

        public FuncArgument(Expression.Identifier.BasicIdentifier func) {
            this.func = func;
        }

        public Expression.Identifier.BasicIdentifier func() {
            return func;
        }

        @Override public void visit(ASTVisitor visitor) {
            visitor.visitFuncArgument(this);
        }
    }

}
