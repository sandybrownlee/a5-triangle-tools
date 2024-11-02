package triangle.ast;

sealed public interface Argument permits Argument.FuncArgument, Argument.VarArgument, Expression {

    record VarArgument(Expression.Identifier identifier) implements Argument { }

    record FuncArgument(String funcName) implements Argument { }

    interface Visitor<ST,T> {
        T visit(ST state, Argument argument);
    }
}
