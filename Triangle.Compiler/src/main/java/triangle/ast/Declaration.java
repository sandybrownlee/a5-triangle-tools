package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    String getName();

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Declaration declaration) throws E;

    }

    record ConstDeclaration(String name, Expression value) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

    record VarDeclaration(String name, Type type) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

    record FuncDeclaration(String func, List<Parameter> parameters, Type returnType, Statement expression)
            implements Declaration {

        @Override public String getName() {
            return func;
        }

    }

    record TypeDeclaration(String name, Type type) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

}
