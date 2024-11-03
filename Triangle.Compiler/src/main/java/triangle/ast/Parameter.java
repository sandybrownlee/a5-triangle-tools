package triangle.ast;

import java.util.List;

sealed public interface Parameter permits Parameter.ConstParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    record ConstParameter(String name, Type type) implements Parameter {

        @Override public String getName() {
            return name;
        }

    }

    record VarParameter(String name, Type type) implements Parameter {

        @Override public String getName() {
            return name;
        }

    }

    record FuncParameter(String callable, List<Parameter> parameters, Type returnType) implements Parameter {

        @Override public String getName() {
            return callable;
        }

    }

    interface Visitor<ST,T,E extends Exception> {
        T visit(ST state, Parameter parameter) throws E;
    }
}
