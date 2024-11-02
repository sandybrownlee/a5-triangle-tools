package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.Program;

@Deprecated public interface ProgramVisitor<TArg, TResult> {

    TResult visitProgram(Program ast, TArg arg);

}
