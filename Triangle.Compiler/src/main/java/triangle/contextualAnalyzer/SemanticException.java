package triangle.contextualAnalyzer;

import triangle.ast.Expression;
import triangle.ast.Expression.Identifier;
import triangle.ast.Expression.LitRecord.RecordField;
import triangle.ast.Parameter;
import triangle.ast.Type;
import triangle.types.RuntimeType;
import triangle.syntacticAnalyzer.SourcePosition;

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

        UndeclaredUse(Type type) {
            super("Undeclared use of type: " + type);
        }

    }

    static final class TypeError extends SemanticException {

        TypeError(SourcePosition sourcePos, RuntimeType left, RuntimeType right) {
            super(sourcePos, "Type mismatch: " + left + " and " + right);
        }

        TypeError(SourcePosition sourcePos, RuntimeType type, String expected) {
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
            super(sourcePos, "Assignment to constant: " + identifier);
        }

    }

    static class DuplicateRecordField extends SemanticException {

        DuplicateRecordField(final SourcePosition sourcePos, final RecordField field) {
            super(sourcePos, "Duplicate record field: " + field);
        }

    }

    static final class DuplicateRecordTypeField extends SemanticException {

        private final Type.RecordType.FieldType fieldType;

        DuplicateRecordTypeField(final SourcePosition sourcePos, final Type.RecordType.FieldType fieldType) {
            this.fieldType = fieldType;
            super(sourcePos, "Duplicate field in record type: " + fieldType.fieldName());
        }

        // we may not always have source positions; e.g, when checking a record field type
        DuplicateRecordTypeField(final Type.RecordType.FieldType fieldType) {
            this.fieldType = fieldType;
            super("Duplicate field in record type: " + fieldType.fieldName());
        }

        Type.RecordType.FieldType getFieldType() {
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

}
