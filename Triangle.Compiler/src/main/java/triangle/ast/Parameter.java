package triangle.ast;

import java.util.List;

sealed public interface Parameter permits Parameter.FuncParameter, Parameter.VarParameter {

    record VarParameter(String name, Type type) implements Parameter { }

    record FuncParameter(String callable, List<Parameter> parameters, Type returnType) implements Parameter { }

    interface Visitor<ST,T,E extends Exception> {
        T visit(ST state, Parameter parameter) throws E;
    }
}
