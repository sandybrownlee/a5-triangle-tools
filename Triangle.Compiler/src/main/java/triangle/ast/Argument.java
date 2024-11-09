package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.RuntimeType;

import java.util.Objects;

sealed public interface Argument extends Typeable permits Argument.FuncArgument, Argument.VarArgument, Expression {

    SourcePosition sourcePos();

    final class VarArgument implements Argument {

        private final SourcePosition        sourcePos;
        private final Expression.Identifier var;
        private       RuntimeType           type;

        public VarArgument(SourcePosition sourcePos, Expression.Identifier var) {
            this.sourcePos = sourcePos;
            this.var = var;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression.Identifier var() {
            return var;
        }

        @Override public int hashCode() {
            return Objects.hash(sourcePos, var);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (VarArgument) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.var, that.var);
        }

        @Override public String toString() {
            return "VarArgument[" + "sourcePos=" + sourcePos + ", " + "var=" + var + ']';
        }

        public void setType(final RuntimeType type) {
            this.type = type;
        }


        @Override public RuntimeType getType() {
            return type;
        }

    }

    final class FuncArgument implements Argument {

        private final SourcePosition        sourcePos;
        private final Expression.Identifier func;
        private       RuntimeType      type;

        public FuncArgument(SourcePosition sourcePos, Expression.Identifier func) {
            this.sourcePos = sourcePos;
            this.func = func;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression.Identifier func() {
            return func;
        }

        @Override public int hashCode() {
            return Objects.hash(sourcePos, func);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (FuncArgument) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.func, that.func);
        }

        @Override public String toString() {
            return "FuncArgument[" + "sourcePos=" + sourcePos + ", " + "func=" + func + ']';
        }

        public void setType(final RuntimeType type) {
            this.type = type;
        }


        @Override public RuntimeType getType() {
            return type;
        }

    }

}
