package triangle.repr;

import java.util.List;

sealed public abstract class Expression extends Argument implements Annotatable.Typeable, Annotatable.SourceLocatable {

    protected SourcePosition sourcePos;
    protected Type           type;

    @Override public void setSourcePosition(final SourcePosition sourcePos) {
        this.sourcePos = sourcePos;
    }

    @Override public SourcePosition sourcePosition() {
        return sourcePos;
    }

    @Override public Type getType() {
        return type;
    }

    @Override public void setType(Type type) {
        this.type = type;
    }

    public sealed static abstract class Identifier extends Expression
            permits Identifier.BasicIdentifier, Identifier.RecordAccess, Identifier.ArraySubscript {

        // this finds the "root" of a (possibly complex) identifier
        // e.g., arr[i] -> root = arr
        //       recx.recy.recz -> root = recx
        // this is needed, for example, to check if a record is a constant or not and for deciding addresses during codegen
        abstract public BasicIdentifier root();

        public static final class BasicIdentifier extends Identifier {

            private final String name;

            public BasicIdentifier(String name) {
                this.name = name;
            }

            @Override public BasicIdentifier root() {
                return this;
            }

            public String name() {
                return name;
            }

        }

        public static final class RecordAccess extends Identifier {

            private final Identifier record;
            private final Identifier field;

            public RecordAccess(Identifier record, Identifier field) {
                this.record = record;
                this.field = field;
            }

            @Override public BasicIdentifier root() {
                return record.root();
            }

            public Identifier record() {
                return record;
            }

            public Identifier field() {
                return field;
            }

        }

        public static final class ArraySubscript extends Identifier {

            private final Identifier array;
            private final Expression subscript;

            public ArraySubscript(Identifier array, Expression subscript) {
                this.array = array;
                this.subscript = subscript;
            }

            @Override public BasicIdentifier root() {
                return array.root();
            }

            public Identifier array() {
                return array;
            }

            public Expression subscript() {
                return subscript;
            }

        }

    }

    public static final class LitBool extends Expression {

        private final boolean        value;
        private       SourcePosition sourcePos;

        public LitBool(boolean value) {
            this.value = value;
            this.type = Type.BOOL_TYPE;
        }

        @Override public void setType(final Type type) {
            if (!(type instanceof Type.PrimType.BoolType)) {
                throw new RuntimeException("Attempted to set type of literal value");
            }
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public boolean value() {
            return value;
        }

    }

    public static final class LitInt extends Expression {

        private final int            value;
        private       SourcePosition sourcePos;

        public LitInt(int value) {
            this.value = value;
            this.type = Type.INT_TYPE;
        }

        @Override public void setType(final Type type) {
            if (!(type instanceof Type.PrimType.IntType)) {
                throw new RuntimeException("Attempted to set type of literal value");
            }
        }

        @Override public void setSourcePosition(final SourcePosition sourcePos) {
            this.sourcePos = sourcePos;
        }

        @Override public SourcePosition sourcePosition() {
            return sourcePos;
        }

        public int value() {
            return value;
        }

    }

    public static final class LitChar extends Expression {

        private final char value;

        public LitChar(char value) {
            this.value = value;
            this.type = Type.CHAR_TYPE;
        }

        @Override public void setType(final Type type) {
            if (!(type instanceof Type.PrimType.CharType)) {
                throw new RuntimeException("Attempted to set type of literal value to incorrect type");
            }
        }

        public char value() {
            return value;
        }

    }

    public static final class LitArray extends Expression {

        private final List<Expression> elements;

        public LitArray(List<Expression> elements) {
            this.elements = elements;
        }

        public List<Expression> elements() {
            return elements;
        }

    }

    public static final class LitRecord extends Expression {

        private final List<RecordField> fields;

        public LitRecord(List<RecordField> fields) {
            this.fields = fields;
        }

        public List<RecordField> fields() {
            return fields;
        }

        public record RecordField(String name, Expression value) { }

    }

    public static final class UnaryOp extends Expression {

        private final Identifier.BasicIdentifier operator;
        private final Expression                 operand;

        public UnaryOp(Identifier.BasicIdentifier operator, Expression operand) {
            this.operator = operator;
            this.operand = operand;
        }

        public Identifier.BasicIdentifier operator() {
            return operator;
        }

        public Expression operand() {
            return operand;
        }

    }

    public static final class BinaryOp extends Expression {

        // TODO: maybe migrate operator type to String?
        private final Identifier.BasicIdentifier operator;
        private final Expression                 leftOperand;
        private final Expression                 rightOperand;

        public BinaryOp(Identifier.BasicIdentifier operator, Expression leftOperand, Expression rightOperand) {
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
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

        public enum BINOPS {
            OR, AND, LTE, GTE, GT, LT, NOT, SUB, ADD, MUL, DIV, MOD, EQ, NEQ
        }

    }

    public static final class LetExpression extends Expression {

        private final List<Declaration> declarations;
        private final Expression        expression;

        public LetExpression(List<Declaration> declarations, Expression expression) {
            this.declarations = declarations;
            this.expression = expression;
        }

        public List<Declaration> declarations() {
            return declarations;
        }

        public Expression expression() {
            return expression;
        }

    }

    public static final class IfExpression extends Expression {

        private final Expression condition;
        private final Expression consequent;
        private final Expression alternative;

        public IfExpression(Expression condition, Expression consequent, Expression alternative) {
            this.condition = condition;
            this.consequent = consequent;
            this.alternative = alternative;
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

    }

    public static final class FunCall extends Expression {

        private final Identifier.BasicIdentifier func;
        private final List<Argument>             arguments;

        public FunCall(Identifier.BasicIdentifier func, List<Argument> arguments) {
            this.func = func;
            this.arguments = arguments;
        }

        public Identifier.BasicIdentifier func() {
            return func;
        }

        public List<Argument> arguments() {
            return arguments;
        }

    }

    public static final class SequenceExpression extends Expression {

        private final Statement  statement;
        private final Expression expression;

        public SequenceExpression(Statement statement, Expression expression) {
            this.statement = statement;
            this.expression = expression;
        }

        public Statement statement() {
            return statement;
        }

        public Expression expression() {
            return expression;
        }

    }

}
