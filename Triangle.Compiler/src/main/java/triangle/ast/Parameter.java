package triangle.ast;

import triangle.types.RuntimeType;

import java.util.List;

sealed public interface Parameter extends Typeable
        permits Parameter.ValueParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    // TODO: remove
    SourcePosition sourcePos();

    final class ValueParameter implements Parameter {

        private final SourcePosition sourcePos;
        private final String         name;
        private       Type           declaredType;
        private       RuntimeType    type;

        public ValueParameter(SourcePosition sourcePos, String name, Type declaredType) {
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

        @Override public String toString() {
            return "ValueParameter[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ", " + "type=" + declaredType + ']';
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
