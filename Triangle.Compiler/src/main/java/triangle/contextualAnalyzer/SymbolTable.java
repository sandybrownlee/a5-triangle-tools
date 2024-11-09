package triangle.contextualAnalyzer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

final class SymbolTable<T> {

    private final Deque<Map<String, T>> scopes = new ArrayDeque<>();

    SymbolTable(Map<String, T> initial) {
        // create a copy to prevent mutability shenanigans
        scopes.push(new HashMap<>(initial));
    }

    void add(String name, T binding) {
        scopes.peek().put(name, binding);
    }

    void enterNewScope() {
        scopes.push(new HashMap<>());
    }

    void exitScope() {
        scopes.pop();
    }

    T lookup(String name) throws NoSuchElementException {
        for (Map<String, T> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        throw new NoSuchElementException();
    }

}
