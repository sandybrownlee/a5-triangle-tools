package triangle.contextualAnalyzer;

import triangle.ast.Expression;
import triangle.ast.Type;
import triangle.syntacticAnalyzer.SourcePosition;

public abstract class SemanticException extends Exception {

    public SemanticException(final String s) {
        super(s);
    }

    public SemanticException(final SourcePosition sourcePos, final String s) {
        super(s + "at" + sourcePos.lineNo() + ":" + sourcePos.colNo());
    }

    static final class UndeclaredUse extends SemanticException {

        UndeclaredUse(final SourcePosition sourcePos, Expression.Identifier identifier) {
            super(sourcePos, "Undeclared use of identifier: " + identifier);
        }

        UndeclaredUse(final SourcePosition sourcePos, Type type) {
            super(sourcePos, "Undeclared use of type: " + type);
        }

        UndeclaredUse(Type type) {
            super("Undeclared use of type: " + type);
        }

    }

    static final class TypeError extends SemanticException {

        TypeError(Type left, Type right) {
            super("Type mismatch: " + left + " got: " + right);
        }

        TypeError(Type type, String expected) {
            super("Unexpected type: " + type + " expecting: " + expected);
        }

    }

    static final class ArityMismatch extends SemanticException {

        ArityMismatch(final SourcePosition sourcePos, Expression callExpression, int expectedArity, int arity) {
            super(sourcePos, "Arity mismatch: " + callExpression + " expected: " + expectedArity + " got: " + arity);
        }

    }

    static final class AssignmentToConstant extends SemanticException {

        AssignmentToConstant(final SourcePosition sourcePos, final Expression.Identifier identifier) {
            super(sourcePos, "Assignment to constant: " + identifier);
        }

    }

}
