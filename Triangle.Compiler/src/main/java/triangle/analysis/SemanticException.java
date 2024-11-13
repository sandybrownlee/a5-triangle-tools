package triangle.analysis;

import triangle.repr.Argument;
import triangle.repr.Expression;
import triangle.repr.Expression.Identifier;
import triangle.repr.Expression.LitRecord.RecordField;
import triangle.repr.Parameter;
import triangle.repr.Type;
import triangle.repr.SourcePosition;
import triangle.repr.TypeSig;

public abstract class SemanticException extends Exception {

    public SemanticException(final String s) {
        super(s);
    }

    public SemanticException(final SourcePosition sourcePos, final String s) {
        this(s + " at " + sourcePos.lineNo() + ":" + sourcePos.colNo());
    }

    static final class UndeclaredUse extends SemanticException {

        UndeclaredUse(final SourcePosition sourcePos, Identifier identifier) {
            super(sourcePos, "Undeclared use of identifier: " + identifier);
        }

        UndeclaredUse(TypeSig typeSig) {
            super("Undeclared use of type: " + typeSig);
        }

    }

    static final class TypeError extends SemanticException {

        TypeError(SourcePosition sourcePos, Type left, Type right) {
            super(sourcePos, "Type mismatch: " + left + " and " + right);
        }

        TypeError(SourcePosition sourcePos, Type type, String expected) {
            super(sourcePos, "Unexpected type: " + type + " expecting: " + expected);
        }

    }

    static final class ArityMismatch extends SemanticException {

        ArityMismatch(final SourcePosition sourcePos, Expression callExpression, int expectedArity, int arity) {
            super(sourcePos, "Arity mismatch: " + callExpression + " expected: " + expectedArity + " got: " + arity);
        }

    }

    static final class AssignmentToConstant extends SemanticException {

        AssignmentToConstant(final SourcePosition sourcePos, final Identifier identifier) {
            super(sourcePos, "Assignment to constant or passing constant as var argument: " + identifier);
        }

    }

    static class DuplicateRecordField extends SemanticException {

        DuplicateRecordField(final SourcePosition sourcePos, final RecordField field) {
            super(sourcePos, "Duplicate record field: " + field);
        }

    }

    static final class DuplicateRecordTypeField extends SemanticException {

        private final TypeSig.RecordTypeSig.FieldType fieldType;

        DuplicateRecordTypeField(final SourcePosition sourcePos, final TypeSig.RecordTypeSig.FieldType fieldType) {
            this.fieldType = fieldType;
            super(sourcePos, "Duplicate field in record type: " + fieldType.fieldName());
        }

        // we may not always have source positions; e.g, when checking a record field typeSig
        DuplicateRecordTypeField(final TypeSig.RecordTypeSig.FieldType fieldType) {
            this.fieldType = fieldType;
            super("Duplicate field in record type: " + fieldType.fieldName());
        }

        TypeSig.RecordTypeSig.FieldType getFieldType() {
            return fieldType;
        }

    }

    static final class DuplicateParameter extends SemanticException {

        DuplicateParameter(final SourcePosition sourcePos, final Parameter parameter) {
            super(sourcePos, "Duplicate parameter: " + parameter);
        }

    }

    static final class FunctionResult extends SemanticException {

        FunctionResult(final SourcePosition sourcePos, final Identifier identifier) {
            super(sourcePos, "Attempted to use a function/procedure as an expression result: " + identifier);
        }

    }

    static final class InvalidArgument extends SemanticException {

        InvalidArgument(final SourcePosition sourcePos, final Argument argument, Class<? extends Argument> expectedClass) {
            super(sourcePos, "Expected argument " + argument + " to be of " + expectedClass);
        }

    }

    static final class IntegerLiteralTooLarge extends SemanticException {

        IntegerLiteralTooLarge(final SourcePosition sourcePos, final int value) {
            super(sourcePos, "Integer literal too large: " + value);
        }

    }

}
