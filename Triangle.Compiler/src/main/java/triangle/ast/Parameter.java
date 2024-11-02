package triangle.ast;

import java.util.List;

sealed public interface Parameter permits Parameter.ConstParameter, Parameter.CallableParameter, Parameter.VarParameter {

    record ConstParameter(String name, Type type) implements Parameter { }

    record VarParameter(String name, Type type) implements Parameter { }

    record CallableParameter(String callable, List<Parameter> parameters, Type returnType) implements Parameter { }

}
