package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    interface Visitor<ST, T> {

        T visit(ST state, Declaration declaration);

    }

    record ConstDeclaration(String name, Expression value) implements Declaration { }

    record VarDeclaration(String name, Type type) implements Declaration { }

    record FuncDeclaration(String callable, List<Parameter> parameters, Type returnType, Statement expression)
            implements Declaration { }

    record TypeDeclaration(String name, Type type) implements Declaration { }

}
