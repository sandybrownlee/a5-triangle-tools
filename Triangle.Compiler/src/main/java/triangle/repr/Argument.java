package triangle.repr;

// TODO: maybe make Argument a sealed class and move sourcePos and type to it?
sealed public interface Argument extends Annotatable.Typeable, Annotatable.SourceLocatable
        permits Argument.FuncArgument, Argument.VarArgument, Expression {

    final class VarArgument implements Argument {

        private final Expression.Identifier var;
        private       SourcePosition        sourcePos;
        private       Type                  type;

        public VarArgument(Expression.Identifier var) {
            this.var = var;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "VarArgument[" + "sourcePos=" + sourcePos + ", " + "var=" + var + ']';
        }

        @Override public Type getType() {
            return type;
        }

        @Override public void setType(final Type type) {
            this.type = type;
        }

        public Expression.Identifier var() {
            return var;
        }

    }

    final class FuncArgument implements Argument {

        private final Expression.Identifier.BasicIdentifier func;
        private       SourcePosition                        sourcePos;
        private       Type                                  type;

        public FuncArgument(Expression.Identifier.BasicIdentifier func) {
            this.func = func;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "FuncArgument[" + "sourcePos=" + sourcePos + ", " + "func=" + func + ']';
        }

        @Override public Type getType() {
            return type;
        }

        @Override public void setType(final Type type) {
            this.type = type;
        }

        public Expression.Identifier.BasicIdentifier func() {
            return func;
        }

    }

}
