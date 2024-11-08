package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.Type;

import java.util.List;

sealed public interface Parameter permits Parameter.ConstParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    SourcePosition sourcePos();

    record ConstParameter(SourcePosition sourcePos, String name, Type type) implements Parameter {

        @Override public String getName() {
            return name;
        }

    }

    record VarParameter(SourcePosition sourcePos, String name, Type type) implements Parameter {

        @Override public String getName() {
            return name;
        }

    }

    record FuncParameter(SourcePosition sourcePos, String callable, List<Parameter> parameters, Type returnType)
            implements Parameter {

        @Override public String getName() {
            return callable;
        }

    }

}
