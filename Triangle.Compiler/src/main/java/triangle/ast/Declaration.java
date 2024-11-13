package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.ProcDeclaration,
                Declaration.TypeDeclaration, Declaration.VarDeclaration {

    record ConstDeclaration(SourcePosition sourcePos, String name, Expression value) implements Declaration { }

    final class VarDeclaration implements Declaration {

        private final SourcePosition sourcePos;
        private final String         name;
        private final Type           declaredType;
        private       RuntimeType    runtimeType;

        public VarDeclaration(SourcePosition sourcePos, String name, Type declaredType) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.declaredType = declaredType;
        }

        public void setRuntimeType(RuntimeType type) {
            this.runtimeType = type;
        }

        @Override public String toString() {
            return "VarDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "declaredType=" + declaredType +
                   ']';
        }

        public String name() {
            return name;
        }

        public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Type declaredType() {
            return this.declaredType;
        }

        public RuntimeType runtimeType() {
            return runtimeType;
        }

    }

    record FuncDeclaration(SourcePosition sourcePos, String name, List<Parameter> parameters, Type declaredReturnType,
                           Expression expression) implements Declaration { }

    record ProcDeclaration(SourcePosition sourcePos, String name, List<Parameter> parameters, Statement statement)
            implements Declaration { }

    record TypeDeclaration(SourcePosition sourcePos, String name, Type type) implements Declaration { }

}
