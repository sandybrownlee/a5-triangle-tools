package triangle.ast;

import java.util.List;

sealed public interface Parameter extends Typeable
        permits Parameter.ValueParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    final class ValueParameter implements Parameter {

        private final String      name;
        private final Type        declaredType;
        private       RuntimeType type;

        public ValueParameter(String name, Type declaredType) {
            this.name = name;
            this.declaredType = declaredType;
        }

        @Override public String getName() {
            return name;
        }

        @Override public String toString() {
            return "ValueParameter[" + ", " + "name=" + name + ", " + "type=" + declaredType + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

        public String name() {
            return name;
        }

        public Type declaredType() {
            return declaredType;
        }

    }

    final class VarParameter implements Parameter {

        private final String      name;
        private final Type        declaredType;
        private       RuntimeType type;

        public VarParameter(String name, Type declaredType) {
            this.name = name;
            this.declaredType = declaredType;
        }

        @Override public String getName() {
            return name;
        }

        @Override public String toString() {
            return "VarParameter[" + ", " + "name=" + name + ", " + "type=" + type + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

        public String name() {
            return name;
        }

        public RuntimeType type() {
            return type;
        }

        public Type declaredType() {
            return declaredType;
        }

    }

    final class FuncParameter implements Parameter {

        private final String          callable;
        private final List<Parameter> parameters;
        private final Type            declaredReturnType;
        private       RuntimeType     returnType;

        public FuncParameter(
                String callable, List<Parameter> parameters, triangle.ast.Type declaredReturnType
        ) {
            this.callable = callable;
            this.parameters = parameters;
            this.declaredReturnType = declaredReturnType;
        }

        @Override public String getName() {
            return callable;
        }

        @Override public String toString() {
            return "FuncParameter[" + ", " + "callable=" + callable + ", " + "parameters=" + parameters + ", " + "returnType=" +
                   returnType + ']';
        }

        @Override public RuntimeType getType() {
            return returnType;
        }

        @Override public void setType(RuntimeType returnType) {
            this.returnType = returnType;
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

    }

}
