package triangle.ast;

import java.util.List;

sealed public interface Expression extends Statement, Argument
        permits Expression.BinaryOp, Expression.FunCall, Expression.Identifier, Expression.IfExpression, Expression.LetExpression,
                Expression.LitArray, Expression.LitBool, Expression.LitChar, Expression.LitInt, Expression.LitRecord,
                Expression.UnaryOp {

    sealed interface Identifier extends Expression permits Identifier.BasicIdentifier, Identifier.RecordAccess,
                                                           Identifier.ArraySubscript {

        record BasicIdentifier(String name) implements Identifier { }

        record RecordAccess(Identifier record, Identifier field) implements Identifier { }

        record ArraySubscript(Identifier array, Expression subscript) implements Identifier { }

        interface Visitor<ST, T,E extends Exception> {
            T visit(ST state, Identifier identifier) throws E;
        }

        // this finds the "root" of a (possibly complex) identifer
        // e.g., arr[i] -> root = arr
        //       recx.recy.recz -> root = recx
        // this is needed, for example, to check if a record is a constant or not
        static BasicIdentifier getRoot(Identifier identifier) {
            return switch (identifier) {
                case ArraySubscript arraySubscript -> getRoot(arraySubscript.array());
                case BasicIdentifier basicIdentifier -> basicIdentifier;
                case RecordAccess recordAccess -> getRoot(recordAccess.record());
            };
        }

    }

    record LitBool(boolean value) implements Expression { }

    record LitInt(int value) implements Expression { }

    record LitChar(char value) implements Expression { }

    record LitArray(List<Expression> elements) implements Expression { }

    record LitRecord(List<RecordField> fields) implements Expression {
        public record RecordField(String name, Expression value) { }
    }

    record UnaryOp(Identifier.BasicIdentifier operator, Expression operand) implements Expression { }

    record BinaryOp(Identifier.BasicIdentifier operator, Expression leftOperand, Expression rightOperand) implements Expression { }

    record LetExpression(List<Declaration> declarations, Expression expression) implements Expression { }

    record IfExpression(Expression condition, Expression consequent, Expression alternative) implements Expression { }

    record FunCall(Identifier callable, List<Argument> arguments) implements Expression { }

    interface Visitor<ST,T,E extends Exception> {
        T visit(ST state, Expression expression) throws E;
    }
}
