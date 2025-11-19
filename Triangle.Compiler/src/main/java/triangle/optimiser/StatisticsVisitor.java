package triangle.optimiser;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.*;
import triangle.abstractSyntaxTrees.aggregates.*;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.*;
import triangle.abstractSyntaxTrees.terminals.*;
import triangle.abstractSyntaxTrees.types.*;
import triangle.abstractSyntaxTrees.vnames.*;
import triangle.abstractSyntaxTrees.visitors.*;
import triangle.syntacticAnalyzer.SourcePosition;

public class StatisticsVisitor implements ProgramVisitor<Void, Void>, DeclarationVisitor<Void, Void>,
        CommandVisitor<Void, Void>, ExpressionVisitor<Void, Void>, VnameVisitor<Void, Void>,
        ActualParameterVisitor<Void, Void>, ActualParameterSequenceVisitor<Void, Void>,
        ArrayAggregateVisitor<Void, Void>, RecordAggregateVisitor<Void, Void>, FormalParameterVisitor<Void, Void>,
        FormalParameterSequenceVisitor<Void, Void>, TypeDenoterVisitor<Void, Void>, IdentifierVisitor<Void, Void>,
        LiteralVisitor<Void, Void>, OperatorVisitor<Void, Void> {

    private int integerExpressionCount = 0;
    private int characterExpressionCount = 0;

    public int getIntegerExpressionCount() {
        return integerExpressionCount;
    }

    public int getCharacterExpressionCount() {
        return characterExpressionCount;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        characterExpressionCount++;
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        integerExpressionCount++;
        return null;
    }

    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.V.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void arg) {
        ast.I.visit(this, null);
        ast.APS.visit(this, null);
        return null;
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ast.E.visit(this, null);
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        ast.D.visit(this, null);
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        ast.E.visit(this, null);
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitLoopCommand(LoopCommand ast, Void arg) {
        ast.C1.visit(this, null);
        ast.E.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Void visitDoubleClickCommand(DoubleClickCommand ast, Void arg) {
        ast.V.visit(this, null);
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        ast.AA.visit(this, null);
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        ast.O.visit(this, null);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        ast.I.visit(this, null);
        ast.APS.visit(this, null);
        return null;
    }

    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression ast, Void arg) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        ast.E3.visit(this, null);
        return null;
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void arg) {
        ast.D.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        ast.RA.visit(this, null);
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.O.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        ast.V.visit(this, null);
        return null;
    }

    @Override
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        return null;
    }

    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        ast.T.visit(this, null);
        ast.FPS.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
        ast.FPS.visit(this, null);
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this, null);
        ast.D2.visit(this, null);
        return null;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclaration ast, Void arg) {
        ast.T.visit(this, null);
        return null;
    }

    @Override
    public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
        ast.T.visit(this, null);
        return null;
    }

    @Override
    public Void visitDotVname(DotVname ast, Void arg) {
        ast.V.visit(this, null);
        ast.I.visit(this, null);
        return null;
    }

    @Override
    public Void visitSimpleVname(SimpleVname ast, Void arg) {
        ast.I.visit(this, null);
        return null;
    }

    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void arg) {
        ast.V.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
        ast.E.visit(this, null);
        ast.AA.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        ast.I.visit(this, null);
        ast.E.visit(this, null);
        ast.RA.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        ast.I.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        ast.I.visit(this, null);
        ast.T.visit(this, null);
        return null;
    }

    @Override
    public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
        ast.T.visit(this, null);
        return null;
    }

    @Override
    public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        ast.I.visit(this, null);
        ast.FPS.visit(this, null);
        return null;
    }

    @Override
    public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        ast.I.visit(this, null);
        ast.T.visit(this, null);
        return null;
    }

    @Override
    public Void visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this, null);
        ast.FPS.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this, null);
        return null;
    }

    @Override
    public Void visitConstActualParameter(ConstActualParameter ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitFuncActualParameter(FuncActualParameter ast, Void arg) {
        ast.I.visit(this, null);
        return null;
    }

    @Override
    public Void visitProcActualParameter(ProcActualParameter ast, Void arg) {
        ast.I.visit(this, null);
        return null;
    }

    @Override
    public Void visitVarActualParameter(VarActualParameter ast, Void arg) {
        ast.V.visit(this, null);
        return null;
    }

    @Override
    public Void visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this, null);
        ast.APS.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this, null);
        return null;
    }

    @Override
    public Void visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
        ast.T.visit(this, null);
        ast.IL.visit(this, null);
        return null;
    }

    @Override
    public Void visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        ast.I.visit(this, null);
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCharacterLiteral(CharacterLiteral ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIdentifier(Identifier ast, Void arg) {
        return null;
    }

    @Override
    public Void visitOperator(Operator ast, Void arg) {
        return null;
    }

    @Override
    public Void visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        ast.FT.visit(this, null);
        return null;
    }

    @Override
    public Void visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
        ast.I.visit(this, null);
        ast.T.visit(this, null);
        ast.FT.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
        ast.I.visit(this, null);
        ast.T.visit(this, null);
        return null;
    }
}