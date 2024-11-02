package triangle.abstractSyntaxTrees.declarations;

import triangle.abstractSyntaxTrees.formals.FormalParameterSequence;
import triangle.abstractSyntaxTrees.types.TypeDenoter;

@Deprecated public interface FunctionDeclaration {

    FormalParameterSequence getFormals();

    TypeDenoter getType();

}
