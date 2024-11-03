package triangle.contextualAnalyzer;

import triangle.ast.AllVisitor;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Program;
import triangle.ast.Statement;
import triangle.ast.Type;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SemanticAnalyzer implements AllVisitor<SemanticAnalyzer.SymbolTable,Type> {

    @Override public Type visit(final SymbolTable symtab, final Argument argument) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Declaration declaration) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Expression.Identifier identifier) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Expression expression) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Parameter parameter) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Program program) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Statement statement) {
        return null;
    }

    @Override public Type visit(final SymbolTable symtab, final Type type) {
        return null;
    }

    public static final class SymbolTable {
        private final Deque<Map<Expression.Identifier.BasicIdentifier, Type>> scopes = new ArrayDeque<>();

        public SymbolTable() {
            scopes.push(new HashMap<>());
        }

        private Optional<Type> lookup(Expression.Identifier.BasicIdentifier identifier) {
            for (Map<Expression.Identifier.BasicIdentifier, Type> scope : scopes) {
                if (scope.containsKey(identifier)) {
                    return Optional.of(scope.get(identifier));
                }
            }

            return Optional.empty();
        }

        private void add(Expression.Identifier.BasicIdentifier identifier, Type type) {
            scopes.peek().put(identifier, type);
        }

        private void enterNewScope() {
            scopes.push(new HashMap<>());
        }

        private void exitScope() {
            scopes.pop();
        }
    }

    public static class UndeclaredUse extends Exception {

        private UndeclaredUse(Expression.Identifier identifier) {
            super("Undeclared use of identifier: " + identifier);
        }

    }

    public static class TypeMismatch extends Exception {

        private TypeMismatch(Type expected, Type found) {
            super("Type mismatch expected: " + expected + " got: " + found);
        }

    }
}
