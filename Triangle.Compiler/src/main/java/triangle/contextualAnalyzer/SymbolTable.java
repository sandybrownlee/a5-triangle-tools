package triangle.contextualAnalyzer;

import triangle.ast.Expression.Identifier.BasicIdentifier;
import triangle.types.Type;
import triangle.types.Type.BasicType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class SymbolTable {

    private final Deque<Env> scopes;

    SymbolTable(Map<BasicType, Type> stdTypes, Map<BasicIdentifier, Binding> stdTerms) {
        scopes = new ArrayDeque<>();

        Map<BasicIdentifierKey, Binding> stdTermsWrapped = new HashMap<>();
        for (Map.Entry<BasicIdentifier, Binding> entry : stdTerms.entrySet()) {
            stdTermsWrapped.put(new BasicIdentifierKey(entry.getKey().name()), entry.getValue());
        }

        scopes.push(new Env(stdTypes, stdTermsWrapped));
    }

    void add(BasicIdentifier identifier, Type type, boolean constant) {
        add(new BasicIdentifierKey(identifier), type, constant);
    }

    Optional<Binding> lookup(BasicIdentifier identifier) {
        return lookup(new BasicIdentifierKey(identifier));
    }

    void enterNewScope() {
        scopes.push(new Env(new HashMap<>(), new HashMap<>()));
    }

    void exitScope() {
        scopes.pop();
    }

    boolean isConstant(BasicIdentifier identifier) {
        return lookup(new BasicIdentifierKey(identifier)).get().constant();
    }

    void add(BasicType basicType, Type type) {
        scopes.peek().types.put(basicType, type);
    }

    Optional<Type> lookup(BasicType type) {
        for (Env env : scopes) {
            if (env.types.containsKey(type)) {
                return Optional.of(env.types.get(type));
            }
        }

        return Optional.empty();
    }

    private Optional<Binding> lookup(BasicIdentifierKey identifier) {
        for (Env env : scopes) {
            if (env.terms.containsKey(identifier)) {
                return Optional.of(env.terms.get(identifier));
            }
        }

        return Optional.empty();
    }

    private void add(BasicIdentifierKey identifier, Type type, boolean constant) {
        scopes.peek().terms.put(identifier, new Binding(type, constant));
    }

    record Binding(Type type, boolean constant) { }

    // wrapper so we that we only compare identifiers on their names
    private record BasicIdentifierKey(String name) {

        BasicIdentifierKey(BasicIdentifier identifier) {
            this(identifier.name());
        }

    }

    private record Env(Map<BasicType, Type> types, Map<BasicIdentifierKey, Binding> terms) { }

}
