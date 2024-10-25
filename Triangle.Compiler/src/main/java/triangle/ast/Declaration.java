package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    record ConstDeclaration(Expression.Identifier name, Expression value) implements Declaration { }

    record VarDeclaration(Expression.Identifier name, Type type) implements Declaration { }

    record FuncDeclaration(Expression.Identifier callable, List<Parameter> parameters) implements Declaration { }

    record TypeDeclaration(Expression.Identifier name, Type type) implements Declaration { }

}
