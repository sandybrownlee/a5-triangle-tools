package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    interface Visitor<ST, T,E extends Exception> {

        T visit(ST state, Declaration declaration) throws E;

    }

    record ConstDeclaration(String name, Expression value) implements Declaration { }

    record VarDeclaration(String name, Type type) implements Declaration { }

    record FuncDeclaration(String func, List<Parameter> parameters, Type returnType, Statement expression)
            implements Declaration { }

    record TypeDeclaration(String name, Type type) implements Declaration { }

}
