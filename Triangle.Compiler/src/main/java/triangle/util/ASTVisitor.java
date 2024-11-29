package triangle.util;

import triangle.repr.*;

// perfunctory implementation of Visitor pattern for task 5;
// only ever used in one place, so no need to parameterize by anything
// only ever used in one place, so no need to return anything other than void
// If we were properly implementing the Visitor pattern, we would probably need multiple different Visitors and have
//      them be parameterized by different things; this is just to show the "core" of the Visitor pattern
public interface ASTVisitor {

    void visitFuncArgument(Argument.FuncArgument funcArgument);

    void visitVarArgument(Argument.VarArgument varArgument);

    void visitConstDeclaration(Declaration.ConstDeclaration constDeclaration);

    void visitVarDeclaration(Declaration.VarDeclaration varDeclaration);

    void visitFuncDeclaration(Declaration.FuncDeclaration funcDeclaration);

    void visitProcDeclaration(Declaration.ProcDeclaration procDeclaration);

    void visitTypeDeclaration(Declaration.TypeDeclaration typeDeclaration);

    void visitBasicIdentifier(Expression.Identifier.BasicIdentifier basicIdentifier);

    void visitRecordAccess(Expression.Identifier.RecordAccess recordAccess);

    void visitArraySubscript(Expression.Identifier.ArraySubscript arraySubscript);

    void visitLitBool(Expression.LitBool litBool);

    void visitLitInt(Expression.LitInt litInt);

    void visitLitChar(Expression.LitChar litChar);

    void visitLitArray(Expression.LitArray litArray);

    void visitLitRecord(Expression.LitRecord litRecord);

    void visitUnaryOp(Expression.UnaryOp unaryOp);

    void visitBinaryOp(Expression.BinaryOp binaryOp);

    void visitLetExpression(Expression.LetExpression letExpression);

    void visitIfExpression(Expression.IfExpression ifExpression);

    void visitFunCall(Expression.FunCall funCall);

    void visitSequenceExpression(Expression.SequenceExpression sequenceExpression);

    void visitValueParameter(Parameter.ValueParameter valueParameter);

    void visitVarParameter(Parameter.VarParameter varParameter);

    void visitFuncParameter(Parameter.FuncParameter funcParameter);

    void visitStatementBlock(Statement.StatementBlock statementBlock);

    void visitLetStatement(Statement.LetStatement letStatement);

    void visitIfStatement(Statement.IfStatement ifStatement);

    void visitWhileStatement(Statement.WhileStatement whileStatement);

    void visitLoopWhileStatement(Statement.LoopWhileStatement loopWhileStatement);

    void visitRepeatWhileStatement(Statement.RepeatWhileStatement repeatWhileStatement);

    void visitRepeatUntilStatement(Statement.RepeatUntilStatement repeatUntilStatement);

    void visitAssignStatement(Statement.AssignStatement assignStatement);

    void visitExpressionStatement(Statement.ExpressionStatement expressionStatement);

    void visitNoopStatement(Statement.NoopStatement noopStatement);

    void visitBasicTypeSig(TypeSig.BasicTypeSig basicTypeSig);

    void visitArrayTypeSig(TypeSig.ArrayTypeSig arrayTypeSig);

    void visitRecordTypeSig(TypeSig.RecordTypeSig recordTypeSig);

    void visitVoid(TypeSig.Void voidTypeSig);
}
