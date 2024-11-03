package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;

import java.util.List;

sealed public interface Parameter permits Parameter.ConstParameter, Parameter.FuncParameter, Parameter.VarParameter {

    String getName();

    SourcePosition sourcePos();

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Parameter parameter) throws E;

    }

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
