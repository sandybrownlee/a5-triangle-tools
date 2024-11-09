package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.RuntimeType;

import java.util.List;
import java.util.Objects;

sealed public interface Parameter extends Typeable
        permits Parameter.ConstParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    SourcePosition sourcePos();

    final class ConstParameter implements Parameter {

        private final SourcePosition sourcePos;
        private final String         name;
        private       Type           declaredType;
        private       RuntimeType    type;

        public ConstParameter(SourcePosition sourcePos, String name, Type declaredType) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.declaredType = declaredType;
        }

        @Override public String getName() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public String name() {
            return name;
        }

        public Type declaredType() {
            return declaredType;
        }

        @Override public int hashCode() {
            return Objects.hash(sourcePos, name, declaredType);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ConstParameter) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.declaredType, that.declaredType);
        }

        @Override public String toString() {
            return "ConstParameter[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + declaredType + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }


        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class VarParameter implements Parameter {

        private final SourcePosition sourcePos;
        private final String         name;
        private final Type           declaredType;
        private       RuntimeType    type;

        public VarParameter(SourcePosition sourcePos, String name, Type declaredType) {
            this.sourcePos = sourcePos;
            this.name = name;
            this.declaredType = declaredType;
        }

        @Override public String getName() {
            return name;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public String name() {
            return name;
        }

        public RuntimeType type() {
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
            var that = (VarParameter) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.name, that.name) && Objects.equals(
                    this.type, that.type);
        }

        @Override public String toString() {
            return "VarParameter[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + type + ']';
        }

        public Type declaredType() {
            return declaredType;
        }

        @Override public RuntimeType getType() {
            return type;
        }


        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class FuncParameter implements Parameter {

        private final SourcePosition  sourcePos;
        private final String          callable;
        private final List<Parameter> parameters;
        private final Type            declaredReturnType;
        private       RuntimeType     returnType;

        public FuncParameter(
                SourcePosition sourcePos, String callable, List<Parameter> parameters,
                triangle.ast.Type declaredReturnType
        ) {
            this.sourcePos = sourcePos;
            this.callable = callable;
            this.parameters = parameters;
            this.declaredReturnType = declaredReturnType;
        }

        @Override public String getName() {
            return callable;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public String callable() {
            return callable;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public Type declaredReturnType() {
            return declaredReturnType;
        }

        @Override public int hashCode() {
            return Objects.hash(sourcePos, callable, parameters, declaredReturnType);
        }

        @Override public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (FuncParameter) obj;
            return Objects.equals(this.sourcePos, that.sourcePos) && Objects.equals(this.callable, that.callable) &&
                   Objects.equals(this.parameters, that.parameters) && Objects.equals(this.returnType, that.returnType);
        }

        @Override public String toString() {
            return "FuncParameter[" + "sourcePos=" + sourcePos + ", " + "callable=" + callable + ", " + "parameters=" +
                   parameters + ", " + "returnType=" + returnType + ']';
        }

        @Override public RuntimeType getType() {
            return returnType;
        }


        @Override public void setType(RuntimeType returnType) {
            this.returnType = returnType;
        }

    }

}
