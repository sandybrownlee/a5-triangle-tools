package triangle.repr;

import java.util.List;

sealed public interface Declaration extends Annotatable.SourceLocatable
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.ProcDeclaration,
                Declaration.TypeDeclaration, Declaration.VarDeclaration {

    final class ConstDeclaration implements Declaration {

        private final String         name;
        private final Expression     value;
        private       SourcePosition sourcePos;

        public ConstDeclaration(String name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "ConstDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "value=" + value + ']';
        }

        public String name() {
            return name;
        }

        public Expression value() {
            return value;
        }

    }

    final class VarDeclaration implements Declaration {

        private final String         name;
        private final TypeSig        typeSig;
        private       SourcePosition sourcePos;
        private       Type           type;

        public VarDeclaration(String name, TypeSig typeSig) {
            this.name = name;
            this.typeSig = typeSig;
        }

        public void setRuntimeType(Type type) {
            this.type = type;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "VarDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "typeSig=" + typeSig + ']';
        }

        public String name() {
            return name;
        }

        public TypeSig declaredType() {
            return this.typeSig;
        }

        public Type runtimeType() {
            return type;
        }

    }

    final class FuncDeclaration implements Declaration {

        private final String          name;
        private final List<Parameter> parameters;
        private final TypeSig         returnTypeSig;
        private final Expression      expression;
        private       SourcePosition  sourcePos;

        public FuncDeclaration(
                String name, List<Parameter> parameters, TypeSig returnTypeSig, Expression expression
        ) {
            this.name = name;
            this.parameters = parameters;
            this.returnTypeSig = returnTypeSig;
            this.expression = expression;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "FuncDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "parameters=" + parameters +
                   ", " + "returnTypeSig=" + returnTypeSig + ", " + "expression=" + expression + ']';
        }

        public String name() {
            return name;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public TypeSig returnTypeSig() {
            return returnTypeSig;
        }

        public Expression expression() {
            return expression;
        }

    }

    final class ProcDeclaration implements Declaration {

        private final String          name;
        private final List<Parameter> parameters;
        private final Statement       statement;
        private       SourcePosition  sourcePos;

        public ProcDeclaration(String name, List<Parameter> parameters, Statement statement) {
            this.name = name;
            this.parameters = parameters;
            this.statement = statement;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "ProcDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "parameters=" + parameters +
                   ", " + "statement=" + statement + ']';
        }

        public String name() {
            return name;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public Statement statement() {
            return statement;
        }

    }

    final class TypeDeclaration implements Declaration {

        private final String         name;
        private final TypeSig        typeSig;
        private       SourcePosition sourcePos;

        public TypeDeclaration(String name, TypeSig typeSig) {
            this.name = name;
            this.typeSig = typeSig;
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        @Override public String toString() {
            return "TypeDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "typeSig=" + typeSig + ']';
        }

        public String name() {
            return name;
        }

        public TypeSig typeSig() {
            return typeSig;
        }

    }

}
