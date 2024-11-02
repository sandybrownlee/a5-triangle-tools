package triangle.ast;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.ProcDeclaration,
                Declaration.TypeDeclaration, Declaration.VarDeclaration {

    record ConstDeclaration(String name, Expression value) implements Declaration { }

    record VarDeclaration(String name, Type type) implements Declaration { }

    record FuncDeclaration(String callable, List<Parameter> parameters, Type returnType, Expression expression)
            implements Declaration { }

    record ProcDeclaration(String proc, List<Parameter> parameters, Statement statement) implements Declaration { }

    record TypeDeclaration(String name, Type type) implements Declaration { }

    interface Visitor {
        void visit(Declaration declaration);
    }
}
