package triangle.ast;

import java.util.List;

sealed public interface Expression extends Statement, Argument
        permits Expression.BinaryOp, Expression.CallExpression, Expression.Identifier, Expression.IfExpression,
                Expression.LetExpression, Expression.LitArray, Expression.LitChar, Expression.LitInt, Expression.LitRecord,
                Expression.UnaryOp {

    record LitInt(int value) implements Expression { }

    record LitChar(char value) implements Expression { }

    record LitArray(List<Expression> values) implements Expression { }

    // TODO: make Map<Identifier, Expression>
    record LitRecord(List<Identifier> fieldNames, List<Expression> values) implements Expression { }

    record UnaryOp(Identifier operator, Expression operand) implements Expression { }

    record BinaryOp(Identifier operator, Expression loperand, Expression roperand) implements Expression { }

    record LetExpression(List<Declaration> declarations, Expression expression) implements Expression { }

    record IfExpression(Expression condition, Expression consequent, Expression alternative) implements Expression { }

    record Identifier(String value) implements Expression, Type { }

    record CallExpression(Identifier callable, List<Argument> arguments) implements Expression { }

}
