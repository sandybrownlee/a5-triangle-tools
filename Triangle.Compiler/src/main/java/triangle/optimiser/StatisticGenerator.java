package triangle.optimiser;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.visitors.ActualParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.ActualParameterVisitor;
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.FormalParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.IdentifierVisitor;
import triangle.abstractSyntaxTrees.visitors.LiteralVisitor;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.abstractSyntaxTrees.visitors.VnameVisitor;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;

public class StatisticGenerator implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
        ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
        CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
        ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
        IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
        OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
        RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
        VnameVisitor<Void, AbstractSyntaxTree> {

    static int integerExpressions = 0;
    static int characterExpressions = 0;

    @Override
    public AbstractSyntaxTree visitConstFormalParameter(ConstFormalParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcFormalParameter(ProcFormalParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarFormalParameter(VarFormalParameter ast, Void arg) {
;
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitDotVname(DotVname ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSubscriptVname(SubscriptVname ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitOperator(Operator ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterLiteral(CharacterLiteral ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerLiteral(IntegerLiteral ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
        ast.CL.visit(this);
        characterExpressions++;
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
        integerExpressions++;
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncDeclaration(FuncDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcDeclaration(ProcDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitTypeDeclaration(TypeDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryOperatorCommand(UnaryOperatorCommand ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallCommand(CallCommand ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitRepeatCommand(RepeatCommand ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLoopCommand(LoopCommand ast, Void unused) {
        return null;
    }

    // TODO uncomment if you've implemented the repeat command
//	@Override
//	public AbstractSyntaxTree visitRepeatCommand(RepeatCommand ast, Void arg) {
//		ast.C.visit(this);
//		AbstractSyntaxTree replacement = ast.E.visit(this);
//		if (replacement != null) {
//			ast.E = (Expression) replacement;
//		}
//		return null;
//	}

    @Override
    public AbstractSyntaxTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstActualParameter(ConstActualParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncActualParameter(FuncActualParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcActualParameter(ProcActualParameter ast, Void arg) {

        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarActualParameter(VarActualParameter ast, Void arg) {

        return null;
    }

    public AbstractSyntaxTree foldBinaryExpression(AbstractSyntaxTree node1, AbstractSyntaxTree node2, Operator o) {
        return null;
    }

}
