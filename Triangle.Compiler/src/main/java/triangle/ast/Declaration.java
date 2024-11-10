package triangle.ast;

import triangle.types.RuntimeType;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.ProcDeclaration,
                Declaration.TypeDeclaration, Declaration.VarDeclaration {

    String name();

    SourcePosition sourcePos();

    final class ConstDeclaration implements Declaration {

        private final SourcePosition sourcePos;
        private final String         name;
        private final Expression     value;

        public ConstDeclaration(SourcePosition sourcePos, String name, Expression value) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.value = value;
        }

        @Override public String name() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression value() {
            return value;
        }

        @Override public String toString() {
            return "ConstDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "value=" + value + ']';
        }

    }

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

        @Override public String name() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Type declaredType() {
            return this.declaredType;
        }

        public RuntimeType runtimeType() {
            return runtimeType;
        }

        public void setRuntimeType(RuntimeType type) {
            this.runtimeType = type;
        }

        @Override public String toString() {
            return "VarDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + declaredType + ']';
        }

    }

    final class FuncDeclaration implements Declaration {

        private final SourcePosition  sourcePos;
        private final String          name;
        private final List<Parameter> parameters;
        private final Expression      expression;
        private final Type            declaredReturnType;

        public FuncDeclaration(
                SourcePosition sourcePos, String name, List<Parameter> parameters, Type declaredReturnType, Expression expression
        ) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.parameters = parameters;
            this.declaredReturnType = declaredReturnType;
            this.expression = expression;
        }

        @Override public String name() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public Type declaredReturnType() {
            return declaredReturnType;
        }

        public Expression expression() {
            return expression;
        }

        @Override public String toString() {
            return "FuncDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "parameters=" + parameters +
                   ", " + "returnType=" + declaredReturnType + ", " + "expression=" + expression + ']';
        }

    }

    final class ProcDeclaration implements Declaration {

        private final SourcePosition  sourcePos;
        private final String          name;
        private final List<Parameter> parameters;
        private final Statement       statement;

        public ProcDeclaration(
                SourcePosition sourcePos, String name, List<Parameter> parameters, Statement statement
        ) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.parameters = parameters;
            this.statement = statement;
        }

        @Override public String name() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public Statement statement() {
            return statement;
        }

        @Override public String toString() {
            return "FuncDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "parameters=" + parameters +
                   ", " + "expression=" + statement + ']';
        }

    }

    final class TypeDeclaration implements Declaration {

        private final SourcePosition sourcePos;
        private final String         name;
        private final Type           type;

        public TypeDeclaration(SourcePosition sourcePos, String name, Type type) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.type = type;
        }

        @Override public String name() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Type type() {
            return type;
        }

        @Override public String toString() {
            return "TypeDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + type + ']';
        }

    }

}
