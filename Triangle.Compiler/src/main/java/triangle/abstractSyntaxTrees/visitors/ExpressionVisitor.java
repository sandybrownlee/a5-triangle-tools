package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.expressions.*;

public interface ExpressionVisitor<TArg, TResult> {

	TResult visitArrayExpression(ArrayExpression ast, TArg arg);

	TResult visitBinaryExpression(BinaryExpression ast, TArg arg);

	TResult visitCallExpression(CallExpression ast, TArg arg);

	TResult visitCharacterExpression(CharacterExpression ast, TArg arg);

	TResult visitEmptyExpression(EmptyExpression ast, TArg arg);

	TResult visitIfExpression(IfExpression ast, TArg arg);

	TResult visitIntegerExpression(IntegerExpression ast, TArg arg);

	TResult visitLetExpression(LetExpression ast, TArg arg);

	TResult visitRecordExpression(RecordExpression ast, TArg arg);

	TResult visitUnaryExpression(UnaryExpression ast, TArg arg);

	TResult visitVnameExpression(VnameExpression ast, TArg arg);

}
