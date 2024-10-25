package triangle.ast;

sealed public interface Argument permits Expression, Argument.FuncArgument, Argument.VarArgument {

    record VarArgument(Expression.Identifier var) implements Argument { }

    record FuncArgument(Expression.Identifier func) implements Argument { }

}
