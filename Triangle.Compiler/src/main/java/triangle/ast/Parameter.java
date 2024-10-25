package triangle.ast;

import java.util.List;

sealed public interface Parameter permits Parameter.ConstParameter, Parameter.FuncParameter, Parameter.VarParameter {

    record ConstParameter(String name, Type type) implements Parameter { }

    record VarParameter(String name, Type type) implements Parameter { }

    record FuncParameter(String callable, List<Parameter> parameters) implements Parameter { }

}
