package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.Type;

import java.util.List;
import java.util.Objects;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

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

        @Override public int hashCode() {
            return Objects.hash(sourcePos, name, value);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ConstDeclaration) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.value, that.value);
        }

        @Override public String toString() {
            return "ConstDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "value=" + value + ']';
        }

    }

    final class VarDeclaration implements Declaration {

        private final SourcePosition sourcePos;
        private final String name;
        private final Type   type;

        public VarDeclaration(SourcePosition sourcePos, String name, Type type) {
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

        @Override public int hashCode() {
            return Objects.hash(sourcePos, name, type);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (VarDeclaration) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.type, that.type);
        }

        @Override public String toString() {
            return "VarDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + type + ']';
        }

    }

    final class FuncDeclaration implements Declaration {

        private final SourcePosition  sourcePos;
        private final String          name;
        private final List<Parameter> parameters;
        private final Statement       expression;
        private final Type            returnType;

        public FuncDeclaration(
                SourcePosition sourcePos, String name, List<Parameter> parameters, Type returnType, Statement expression
        ) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
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

        public Type returnType() {
            return returnType;
        }

        public Statement expression() {
            return expression;
        }

        @Override public int hashCode() {
            return Objects.hash(sourcePos, name, parameters, returnType, expression);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (FuncDeclaration) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.parameters, that.parameters) && Objects.equals(this.returnType, that.returnType) && Objects.equals(
                    this.expression, that.expression);
        }

        @Override public String toString() {
            return "FuncDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "parameters=" + parameters +
                   ", " + "returnType=" + returnType + ", " + "expression=" + expression + ']';
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

        @Override public int hashCode() {
            return Objects.hash(sourcePos, name, type);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (TypeDeclaration) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.type, that.type);
        }

        @Override public String toString() {
            return "TypeDeclaration[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + type + ']';
        }

    }

}
