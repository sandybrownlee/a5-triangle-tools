package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;

sealed public interface Argument permits Argument.FuncArgument, Argument.VarArgument, Expression {

    SourcePosition sourcePos();

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Argument argument) throws E;

    }

    record VarArgument(SourcePosition sourcePos, Expression.Identifier var) implements Argument { }

    record FuncArgument(SourcePosition sourcePos, Expression.Identifier func) implements Argument { }

}
