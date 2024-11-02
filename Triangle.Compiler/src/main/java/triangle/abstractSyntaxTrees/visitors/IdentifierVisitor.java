package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.terminals.Identifier;

@Deprecated public interface IdentifierVisitor<TArg, TResult> {

    TResult visitIdentifier(Identifier ast, TArg arg);

}
