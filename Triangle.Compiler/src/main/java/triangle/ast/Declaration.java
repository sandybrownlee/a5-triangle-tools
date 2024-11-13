package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.ProcDeclaration,
                Declaration.TypeDeclaration, Declaration.VarDeclaration {

    record ConstDeclaration(SourcePosition sourcePos, String name, Expression value) implements Declaration { }

    final class VarDeclaration implements Declaration {

        private final SourcePosition sourcePos;
        private final String      name;
        private final TypeSig typeSig;
        private       Type    type;

        public VarDeclaration(SourcePosition sourcePos, String name, TypeSig typeSig) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.typeSig = typeSig;
        }

        public void setRuntimeType(Type type) {
            this.type = type;
        }

        @Override public String toString() {
            return "VarDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "typeSig=" + typeSig +
                   ']';
        }

        public String name() {
            return name;
        }

        public SourcePosition sourcePos() {
            return sourcePos;
        }

        public TypeSig declaredType() {
            return this.typeSig;
        }

        public Type runtimeType() {
            return type;
        }

    }

    record FuncDeclaration(SourcePosition sourcePos, String name, List<Parameter> parameters, TypeSig returnTypeSig,
                           Expression expression) implements Declaration { }

    record ProcDeclaration(SourcePosition sourcePos, String name, List<Parameter> parameters, Statement statement)
            implements Declaration { }

    record TypeDeclaration(SourcePosition sourcePos, String name, TypeSig typeSig) implements Declaration { }

}
