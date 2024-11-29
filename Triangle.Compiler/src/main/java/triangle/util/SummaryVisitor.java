package triangle.util;

import triangle.repr.*;

// uses "proper" GoF style Visitor pattern
public class SummaryVisitor implements ASTVisitor {

    private int whileStatements = 0;
    private int ifStatements = 0;
    private int binaryOps = 0;

    @Override
    public void visitFuncArgument(Argument.FuncArgument funcArgument) {
        funcArgument.func().visit(this);
    }

    @Override
    public void visitVarArgument(Argument.VarArgument varArgument) {
        varArgument.var().visit(this);
    }

    @Override
    public void visitConstDeclaration(Declaration.ConstDeclaration constDeclaration) {
        constDeclaration.value().visit(this);
    }

    @Override
    public void visitVarDeclaration(Declaration.VarDeclaration varDeclaration) {
        varDeclaration.declaredType().visit(this);
    }

    @Override
    public void visitFuncDeclaration(Declaration.FuncDeclaration funcDeclaration) {
        funcDeclaration.parameters().forEach(parameter -> parameter.visit(this));
        funcDeclaration.returnTypeSig().visit(this);
        funcDeclaration.expression().visit(this);
    }

    @Override
    public void visitProcDeclaration(Declaration.ProcDeclaration procDeclaration) {
        procDeclaration.parameters().forEach(parameter -> parameter.visit(this));
        procDeclaration.statement().visit(this);
    }

    @Override
    public void visitTypeDeclaration(Declaration.TypeDeclaration typeDeclaration) {
        typeDeclaration.typeSig().visit(this);
    }

    @Override
    public void visitBasicIdentifier(Expression.Identifier.BasicIdentifier basicIdentifier) { }

    @Override
    public void visitRecordAccess(Expression.Identifier.RecordAccess recordAccess) {
        recordAccess.record().visit(this);
        recordAccess.field().visit(this);
    }

    @Override
    public void visitArraySubscript(Expression.Identifier.ArraySubscript arraySubscript) {
        arraySubscript.array().visit(this);
        arraySubscript.subscript().visit(this);
    }

    @Override
    public void visitLitBool(Expression.LitBool litBool) { }

    @Override
    public void visitLitInt(Expression.LitInt litInt) { }

    @Override
    public void visitLitChar(Expression.LitChar litChar) { }

    @Override
    public void visitLitArray(Expression.LitArray litArray) {
        litArray.elements().forEach(element -> element.visit(this));
    }

    @Override
    public void visitLitRecord(Expression.LitRecord litRecord) {
        litRecord.fields().forEach(field -> field.value().visit(this));
    }

    @Override
    public void visitUnaryOp(Expression.UnaryOp unaryOp) {
        unaryOp.operand().visit(this);
    }

    @Override
    public void visitBinaryOp(Expression.BinaryOp binaryOp) {
        binaryOps++;
        binaryOp.leftOperand().visit(this);
        binaryOp.rightOperand().visit(this);
    }

    @Override
    public void visitLetExpression(Expression.LetExpression letExpression) {
        letExpression.declarations().forEach(declaration -> declaration.visit(this));
        letExpression.expression().visit(this);
    }

    @Override
    public void visitIfExpression(Expression.IfExpression ifExpression) {
        ifStatements++;
        ifExpression.condition().visit(this);
        ifExpression.consequent().visit(this);
        ifExpression.alternative().visit(this);
    }

    @Override
    public void visitFunCall(Expression.FunCall funCall) {
        funCall.func().visit(this);
        funCall.arguments().forEach(argument -> argument.visit(this));
    }

    @Override
    public void visitSequenceExpression(Expression.SequenceExpression sequenceExpression) {
        sequenceExpression.statement().visit(this);
        sequenceExpression.expression().visit(this);
    }

    @Override
    public void visitValueParameter(Parameter.ValueParameter valueParameter) {
         valueParameter.declaredType().visit(this);
    }

    @Override
    public void visitVarParameter(Parameter.VarParameter varParameter) {
         varParameter.declaredType().visit(this);
    }

    @Override
    public void visitFuncParameter(Parameter.FuncParameter funcParameter) {
        funcParameter.parameters().forEach(parameter -> parameter.visit(this));
         funcParameter.declaredReturnType().visit(this);
    }

    @Override
    public void visitStatementBlock(Statement.StatementBlock statementBlock) {
        statementBlock.statements().forEach(statement -> statement.visit(this));
    }

    @Override
    public void visitLetStatement(Statement.LetStatement letStatement) {
        letStatement.declarations().forEach(declaration -> declaration.visit(this));
        letStatement.statement().visit(this);
    }

    @Override
    public void visitIfStatement(Statement.IfStatement ifStatement) {
        ifStatements++;
        ifStatement.condition().visit(this);
        ifStatement.consequent().ifPresent(s -> s.visit(this));
        ifStatement.alternative().ifPresent(s -> s.visit(this));
    }

    @Override
    public void visitWhileStatement(Statement.WhileStatement whileStatement) {
        whileStatements++;
        whileStatement.condition().visit(this);
        whileStatement.body().visit(this);
    }

    @Override
    public void visitLoopWhileStatement(Statement.LoopWhileStatement loopWhileStatement) {
        loopWhileStatement.loopBody().visit(this);
        loopWhileStatement.condition().visit(this);
        loopWhileStatement.doBody().visit(this);
    }

    @Override
    public void visitRepeatWhileStatement(Statement.RepeatWhileStatement repeatWhileStatement) {
        repeatWhileStatement.condition().visit(this);
        repeatWhileStatement.body().visit(this);
    }

    @Override
    public void visitRepeatUntilStatement(Statement.RepeatUntilStatement repeatUntilStatement) {
        repeatUntilStatement.condition().visit(this);
        repeatUntilStatement.body().visit(this);
    }

    @Override
    public void visitAssignStatement(Statement.AssignStatement assignStatement) {
        assignStatement.expression().visit(this);
        assignStatement.identifier().visit(this);
    }

    @Override
    public void visitExpressionStatement(Statement.ExpressionStatement expressionStatement) {
        expressionStatement.expression().visit(this);
    }

    @Override
    public void visitNoopStatement(Statement.NoopStatement noopStatement) { }

    @Override
    public void visitBasicTypeSig(TypeSig.BasicTypeSig basicTypeSig) { }

    @Override
    public void visitArrayTypeSig(TypeSig.ArrayTypeSig arrayTypeSig) {
        arrayTypeSig.elementTypeSig().visit(this);
    }

    @Override
    public void visitRecordTypeSig(TypeSig.RecordTypeSig recordTypeSig) {
        recordTypeSig.fieldTypes().forEach(fieldType -> fieldType.fieldTypeSig().visit(this));
    }

    @Override
    public void visitVoid(TypeSig.Void voidTypeSig) { }

    public Summary generateSummary(Statement program) {
        program.visit(this);

        Summary summary = new Summary(whileStatements, ifStatements, binaryOps);

        // defensively reset, in case someone wants to reuse this instance
        whileStatements = 0;
        ifStatements = 0;
        binaryOps = 0;

        return summary;
    }

    public record Summary(int whileStatements, int ifStatements, int binaryOps) {
    }
}
