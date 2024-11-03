package triangle.ast;

sealed public interface Argument permits Argument.FuncArgument, Argument.VarArgument, Expression {

    record VarArgument(Expression.Identifier var) implements Argument { }

    record FuncArgument(Expression.Identifier func) implements Argument { }

    interface Visitor<ST,T,E extends Exception> {
        T visit(ST state, Argument argument) throws E;
    }
}
