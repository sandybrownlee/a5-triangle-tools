package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;

import java.util.List;

sealed public interface Expression extends Statement, Argument
        permits Expression.BinaryOp, Expression.FunCall, Expression.Identifier, Expression.IfExpression, Expression.LetExpression,
                Expression.LitArray, Expression.LitBool, Expression.LitChar, Expression.LitInt, Expression.LitRecord,
                Expression.UnaryOp {

    SourcePosition sourcePos();

    sealed interface Identifier extends Expression permits Identifier.BasicIdentifier, Identifier.RecordAccess,
                                                           Identifier.ArraySubscript {

        // this finds the "root" of a (possibly complex) identifer
        // e.g., arr[i] -> root = arr
        //       recx.recy.recz -> root = recx
        // this is needed, for example, to check if a record is a constant or not
        BasicIdentifier root();

        SourcePosition sourcePos();

        interface Visitor<ST, T, E extends Exception> {

            T visit(ST state, Identifier identifier) throws E;

        }

        record BasicIdentifier(SourcePosition sourcePos, String name) implements Identifier {

            @Override public BasicIdentifier root() {
                return this;
            }

        }

        record RecordAccess(SourcePosition sourcePos, Identifier record, Identifier field) implements Identifier {

            @Override public BasicIdentifier root() {
                return record.root();
            }

        }

        record ArraySubscript(SourcePosition sourcePos, Identifier array, Expression subscript) implements Identifier {

            @Override public BasicIdentifier root() {
                return array.root();
            }

        }

    }

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Expression expression) throws E;

    }

    record LitBool(SourcePosition sourcePos, boolean value) implements Expression { }

    record LitInt(SourcePosition sourcePos, int value) implements Expression { }

    record LitChar(SourcePosition sourcePos, char value) implements Expression { }

    record LitArray(SourcePosition sourcePos, List<Expression> elements) implements Expression { }

    record LitRecord(SourcePosition sourcePos, List<RecordField> fields) implements Expression {

        public record RecordField(String name, Expression value) { }

    }

    record UnaryOp(SourcePosition sourcePos, Identifier.BasicIdentifier operator, Expression operand) implements Expression { }

    record BinaryOp(SourcePosition sourcePos, Identifier.BasicIdentifier operator, Expression leftOperand,
                    Expression rightOperand)
            implements Expression { }

    record LetExpression(SourcePosition sourcePos, List<Declaration> declarations, Expression expression)
            implements Expression { }

    record IfExpression(SourcePosition sourcePos, Expression condition, Expression consequent, Expression alternative)
            implements Expression { }

    record FunCall(SourcePosition sourcePos, Identifier callable, List<Argument> arguments) implements Expression { }

}
