package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    String getName();

    SourcePosition sourcePos();

    record ConstDeclaration(SourcePosition sourcePos, String name, Expression value) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

    record VarDeclaration(SourcePosition sourcePos, String name, Type type) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

    record FuncDeclaration(SourcePosition sourcePos, String func, List<Parameter> parameters, Type returnType,
                           Statement expression)
            implements Declaration {

        @Override public String getName() {
            return func;
        }

    }

    record TypeDeclaration(SourcePosition sourcePos, String name, Type type) implements Declaration {

        @Override public String getName() {
            return name;
        }

    }

}
