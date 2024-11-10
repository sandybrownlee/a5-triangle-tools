package triangle.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

// this is a naive implementation of a symbol table; if compiler performance becomes an issue, this is probably the place to start
public final class SymbolTable<T, S> {

    // ArrayDeque does not permit null elements + LinkedList is fast for prepend/pop operations which we'll be doing a lot of
    private final Deque<Map<String, T>> scopes          = new LinkedList<>();
    private final Deque<S>              scopeLocalState = new LinkedList<>();

    public SymbolTable(S initialState) {
        scopes.push(new HashMap<>());
        scopeLocalState.push(initialState);
    }

    public SymbolTable(Map<String, T> initialScope, S initialState) {
        // create a copy to prevent mutability shenanigans
        scopes.push(new HashMap<>(initialScope));
        scopeLocalState.push(initialState);
    }

    public void setScopeLocalState(S newState) {
        scopeLocalState.pop();
        scopeLocalState.push(newState);
    }

    public void add(String name, T binding) {
        assert scopes.peek() != null;
        scopes.peek().put(name, binding);
    }

    public void enterNewScope(S newState) {
        scopes.push(new HashMap<>());
        scopeLocalState.push(newState);
    }

    public void exitScope() {
        scopes.pop();
    }

    public T lookup(String name) throws NoSuchElementException {
        for (Map<String, T> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }

        throw new NoSuchElementException();
    }

    public List<T> lookupAll(String name) {
        List<T> list = new ArrayList<>();

        for (Map<String, T> scope : scopes) {
            if (scope.containsKey(name)) {
                list.add(scope.get(name));
            }
        }

        return list;
    }

    public DepthLookup lookupWithDepth(String name) throws NoSuchElementException {
        int depth = 0;
        for (Map<String, T> scope : scopes) {
            if (scope.containsKey(name)) {
                return new DepthLookup(depth, scope.get(name));
            }

            depth++;
        }

        throw new NoSuchElementException();
    }

    public S scopeLocalState() {
        assert scopeLocalState.peek() != null;
        return scopeLocalState.peek();
    }

    // result of a lookupWithDepth
    public final class DepthLookup {

        private final int depth;
        private final T   t;

        DepthLookup(int depth, T t) {
            this.depth = depth;
            this.t = t;
        }

        @Override public String toString() {
            return "DepthLookup[" + "depth=" + depth + ", " + "t=" + t + ']';
        }

        public int depth() {
            return depth;
        }

        public T t() {
            return t;
        }

    }

}
