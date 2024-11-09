package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.RuntimeType;

import java.util.List;

sealed public interface Expression extends Argument, Typeable
        permits Expression.BinaryOp, Expression.FunCall, Expression.Identifier, Expression.IfExpression, Expression.LetExpression,
                Expression.LitArray, Expression.LitBool, Expression.LitChar, Expression.LitInt, Expression.LitRecord,
                Expression.UnaryOp {

    sealed interface Identifier extends Expression, Typeable
            permits Identifier.BasicIdentifier, Identifier.RecordAccess, Identifier.ArraySubscript {

        // this finds the "root" of a (possibly complex) identifer
        // e.g., arr[i] -> root = arr
        //       recx.recy.recz -> root = recx
        // this is needed, for example, to check if a record is a constant or not
        BasicIdentifier root();

        final class BasicIdentifier implements Identifier {

            private final SourcePosition sourcePos;
            private final String         name;
            private       RuntimeType    type;
            private       Declaration    declaration;

            private BasicIdentifier(SourcePosition sourcePos, String name, RuntimeType type, Declaration declaration) {
                this.sourcePos = sourcePos;
                this.name = name;
                this.type = type;
                this.declaration = declaration;
            }

            public BasicIdentifier(SourcePosition sourcePos, String name) {
                this(sourcePos, name, null, null);
            }

            // needed when setting up stdenv
            public BasicIdentifier(String name, RuntimeType type) {
                this(null, name, type, null);
            }

            @Override public BasicIdentifier root() {
                return this;
            }

            @Override public SourcePosition sourcePos() {
                return sourcePos;
            }

            public String name() {
                return name;
            }

            @Override public String toString() {
                return "BasicIdentifier[" + "sourcePos=" + sourcePos + ", " + "name=" + name + ']';
            }

            public void setDeclaration(Declaration declaration) {
                this.declaration = declaration;
            }

            @Override public void setType(RuntimeType type) {
                this.type = type;
            }


            @Override public RuntimeType getType() {
                return type;
            }

        }

        final class RecordAccess implements Identifier {

            private final SourcePosition sourcePos;
            private final Identifier     record;
            private final Identifier     field;
            private       RuntimeType    type;

            public RecordAccess(SourcePosition sourcePos, Identifier record, Identifier field) {
                this.sourcePos = sourcePos;
                this.record = record;
                this.field = field;
            }

            @Override public BasicIdentifier root() {
                return record.root();
            }

            @Override public SourcePosition sourcePos() {
                return sourcePos;
            }

            public Identifier record() {
                return record;
            }

            public Identifier field() {
                return field;
            }

            @Override public String toString() {
                return "RecordAccess[" + "sourcePos=" + sourcePos + ", " + "record=" + record + ", " + "field=" + field + ']';
            }

            public void setType(RuntimeType type) {
                this.type = type;
            }


            @Override public RuntimeType getType() {
                return type;
            }

        }

        final class ArraySubscript implements Identifier {

            private final SourcePosition sourcePos;
            private final Identifier     array;
            private final Expression     subscript;
            private       RuntimeType    type;

            public ArraySubscript(SourcePosition sourcePos, Identifier array, Expression subscript) {
                this.sourcePos = sourcePos;
                this.array = array;
                this.subscript = subscript;
            }

            @Override public BasicIdentifier root() {
                return array.root();
            }

            @Override public SourcePosition sourcePos() {
                return sourcePos;
            }

            public Identifier array() {
                return array;
            }

            public Expression subscript() {
                return subscript;
            }

            @Override public String toString() {
                return "ArraySubscript[" + "sourcePos=" + sourcePos + ", " + "array=" + array + ", " + "subscript=" + subscript +
                       ']';
            }

            public void setType(RuntimeType type) {
                this.type = type;
            }

            @Override public RuntimeType getType() {
                return type;
            }

        }

    }

    final class LitBool implements Expression {

        private final SourcePosition sourcePos;
        private final boolean        value;

        public LitBool(SourcePosition sourcePos, boolean value) {
            this.sourcePos = sourcePos;
            this.value = value;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public boolean value() {
            return value;
        }

        @Override public String toString() {
            return "LitBool[" + "sourcePos=" + sourcePos + ", " + "value=" + value + ']';
        }

        @Override public RuntimeType getType() {
            return RuntimeType.BOOL_TYPE;
        }

        @Override public void setType(final RuntimeType type) {
            throw new RuntimeException("Attempted to set type of literal value");
        }

    }

    final class LitInt implements Expression {

        private final SourcePosition sourcePos;
        private final int            value;

        public LitInt(SourcePosition sourcePos, int value) {
            this.sourcePos = sourcePos;
            this.value = value;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public int value() {
            return value;
        }

        @Override public String toString() {
            return "LitInt[" + "sourcePos=" + sourcePos + ", " + "value=" + value + ']';
        }

        @Override public RuntimeType getType() {
            return RuntimeType.INT_TYPE;
        }

        @Override public void setType(final RuntimeType type) {
            throw new RuntimeException("Attempted to set type of literal value");
        }

    }

    final class LitChar implements Expression {

        private final SourcePosition sourcePos;
        private final char           value;

        public LitChar(SourcePosition sourcePos, char value) {
            this.sourcePos = sourcePos;
            this.value = value;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public char value() {
            return value;
        }

        @Override public String toString() {
            return "LitChar[" + "sourcePos=" + sourcePos + ", " + "value=" + value + ']';
        }

        @Override public RuntimeType getType() {
            return RuntimeType.CHAR_TYPE;
        }

        @Override public void setType(final RuntimeType type) {
            throw new RuntimeException("Attempted to set type of literal value");
        }

    }

    final class LitArray implements Expression, Typeable {

        private final SourcePosition   sourcePos;
        private final List<Expression> elements;
        private       RuntimeType      type;

        public LitArray(SourcePosition sourcePos, List<Expression> elements) {
            this.sourcePos = sourcePos;
            this.elements = elements;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Expression> elements() {
            return elements;
        }

        @Override public String toString() {
            return "LitArray[" + "sourcePos=" + sourcePos + ", " + "elements=" + elements + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class LitRecord implements Expression, Typeable {

        private final SourcePosition    sourcePos;
        private final List<RecordField> fields;
        private       RuntimeType       type;

        public LitRecord(SourcePosition sourcePos, List<RecordField> fields) {
            this.sourcePos = sourcePos;
            this.fields = fields;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<RecordField> fields() {
            return fields;
        }

        @Override public String toString() {
            return "LitRecord[" + "sourcePos=" + sourcePos + ", " + "fields=" + fields + ']';
        }

        public record RecordField(String name, Expression value) { }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class UnaryOp implements Expression, Typeable {

        private final SourcePosition             sourcePos;
        private final Identifier.BasicIdentifier operator;
        private final Expression                 operand;
        private       RuntimeType                type;

        public UnaryOp(SourcePosition sourcePos, Identifier.BasicIdentifier operator, Expression operand) {
            this.sourcePos = sourcePos;
            this.operator = operator;
            this.operand = operand;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Identifier.BasicIdentifier operator() {
            return operator;
        }

        public Expression operand() {
            return operand;
        }

        @Override public String toString() {
            return "UnaryOp[" + "sourcePos=" + sourcePos + ", " + "operator=" + operator + ", " + "operand=" + operand + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class BinaryOp implements Expression, Typeable {

        private final SourcePosition             sourcePos;
        private final Identifier.BasicIdentifier operator;
        private final Expression                 leftOperand;
        private final Expression                 rightOperand;
        private       RuntimeType                type;

        public BinaryOp(
                SourcePosition sourcePos, Identifier.BasicIdentifier operator, Expression leftOperand, Expression rightOperand
        ) {
            this.sourcePos = sourcePos;
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Identifier.BasicIdentifier operator() {
            return operator;
        }

        public Expression leftOperand() {
            return leftOperand;
        }

        public Expression rightOperand() {
            return rightOperand;
        }

        @Override public String toString() {
            return "BinaryOp[" + "sourcePos=" + sourcePos + ", " + "operator=" + operator + ", " + "leftOperand=" + leftOperand +
                   ", " + "rightOperand=" + rightOperand + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class LetExpression implements Expression, Typeable {

        private final SourcePosition    sourcePos;
        private final List<Declaration> declarations;
        private final Expression        expression;
        private       RuntimeType       type;

        public LetExpression(SourcePosition sourcePos, List<Declaration> declarations, Expression expression) {
            this.sourcePos = sourcePos;
            this.declarations = declarations;
            this.expression = expression;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public List<Declaration> declarations() {
            return declarations;
        }

        public Expression expression() {
            return expression;
        }

        @Override public String toString() {
            return "LetExpression[" + "sourcePos=" + sourcePos + ", " + "declarations=" + declarations + ", " + "expression=" +
                   expression + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class IfExpression implements Expression, Typeable {

        private final SourcePosition sourcePos;
        private final Expression     condition;
        private final Expression     consequent;
        private final Expression     alternative;
        private       RuntimeType    type;

        public IfExpression(SourcePosition sourcePos, Expression condition, Expression consequent, Expression alternative) {
            this.sourcePos = sourcePos;
            this.condition = condition;
            this.consequent = consequent;
            this.alternative = alternative;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Expression condition() {
            return condition;
        }

        public Expression consequent() {
            return consequent;
        }

        public Expression alternative() {
            return alternative;
        }

        @Override public String toString() {
            return "IfExpression[" + "sourcePos=" + sourcePos + ", " + "condition=" + condition + ", " + "consequent=" +
                   consequent + ", " + "alternative=" + alternative + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

    final class FunCall implements Expression, Typeable {

        private final SourcePosition sourcePos;
        private final Identifier     callable;
        private final List<Argument> arguments;
        private       RuntimeType    type;

        public FunCall(SourcePosition sourcePos, Identifier callable, List<Argument> arguments) {
            this.sourcePos = sourcePos;
            this.callable = callable;
            this.arguments = arguments;
        }

        @Override public SourcePosition sourcePos() {
            return sourcePos;
        }

        public Identifier callable() {
            return callable;
        }

        public List<Argument> arguments() {
            return arguments;
        }

        @Override public String toString() {
            return "FunCall[" + "sourcePos=" + sourcePos + ", " + "callable=" + callable + ", " + "arguments=" + arguments + ']';
        }

        @Override public RuntimeType getType() {
            return type;
        }

        @Override public void setType(RuntimeType type) {
            this.type = type;
        }

    }

}
