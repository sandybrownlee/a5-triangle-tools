package triangle.contextualAnalyzer;

import triangle.ast.Declaration;
import triangle.types.Type;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class SymbolTable {

    private final Deque<Env> scopes;

    SymbolTable(Map<String, Type> stdTypes, Map<String, Binding> stdTerms) {
        scopes = new ArrayDeque<>();

        Map<String, Binding> stdTermsWrapped = new HashMap<>(stdTerms);

        scopes.push(new Env(stdTypes, stdTermsWrapped));
    }

    void addTerm(String name, Type type, boolean constant, Declaration declaration) {
        scopes.peek().terms.put(name, new Binding(type, constant, declaration));
    }

    void enterNewScope() {
        scopes.push(new Env(new HashMap<>(), new HashMap<>()));
    }

    void exitScope() {
        scopes.pop();
    }

    void addType(String typeName, Type type) {
        scopes.peek().types.put(typeName, type);
    }

    Optional<Type> lookupType(String typeName) {
        for (Env env : scopes) {
            if (env.types.containsKey(typeName)) {
                return Optional.of(env.types.get(typeName));
            }
        }

        return Optional.empty();
    }

    Optional<Binding> lookupTerm(String name) {
        for (Env env : scopes) {
            if (env.terms.containsKey(name)) {
                return Optional.of(env.terms.get(name));
            }
        }

        return Optional.empty();
    }

    record Binding(Type type, boolean constant, Declaration declaration) { }

    private record Env(Map<String, Type> types, Map<String, Binding> terms) { }

}
