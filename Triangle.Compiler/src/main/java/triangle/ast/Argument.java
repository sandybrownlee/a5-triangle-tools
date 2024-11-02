package triangle.ast;

sealed public interface Argument permits Argument.ExprArgument, Argument.FuncArgument, Argument.VarArgument, Expression {

    record VarArgument(Expression.Identifier identifier) implements Argument { }

    record FuncArgument(String funcName) implements Argument { }

    record ExprArgument(Expression expression) implements Argument { }

    interface Visitor<ST,T> {
        T visit(ST state, Argument argument);
    }
}
