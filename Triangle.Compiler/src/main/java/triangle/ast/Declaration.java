package triangle.ast;

import triangle.syntacticAnalyzer.SourcePosition;
import triangle.types.Type;

import java.util.List;

sealed public interface Declaration
        permits Declaration.ConstDeclaration, Declaration.FuncDeclaration, Declaration.TypeDeclaration,
                Declaration.VarDeclaration {

    String name();

    SourcePosition sourcePos();

    record ConstDeclaration(SourcePosition sourcePos, String name, Expression value) implements Declaration { }

    record VarDeclaration(SourcePosition sourcePos, String name, Type type) implements Declaration { }

    record FuncDeclaration(SourcePosition sourcePos, String name, List<Parameter> parameters, Type returnType,
                           Statement expression)
            implements Declaration { }

    record TypeDeclaration(SourcePosition sourcePos, String name, Type type) implements Declaration { }

}
