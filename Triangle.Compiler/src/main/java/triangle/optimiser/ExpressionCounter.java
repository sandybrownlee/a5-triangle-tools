package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.*;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.*;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.*;
import triangle.abstractSyntaxTrees.visitors.*;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;

public class ExpressionCounter implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
        ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
        CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
        ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
        IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
        OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
        RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
        VnameVisitor<Void, AbstractSyntaxTree>

{
    @Override
    public AbstractSyntaxTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstActualParameter(ConstActualParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncActualParameter(FuncActualParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcActualParameter(ProcActualParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarActualParameter(VarActualParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallCommand(CallCommand ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyCommand(EmptyCommand ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncDeclaration(FuncDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcDeclaration(ProcDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitTypeDeclaration(TypeDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstFormalParameter(ConstFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitFuncFormalParameter(FuncFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitProcFormalParameter(ProcFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarFormalParameter(VarFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIdentifier(Identifier ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterLiteral(CharacterLiteral ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerLiteral(IntegerLiteral ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitOperator(Operator ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharTypeDenoter(CharTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntTypeDenoter(IntTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitDotVname(DotVname ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSubscriptVname(SubscriptVname ast, Void unused) {
        return null;
    }

    private int characterCount = 0;
    private int integerCount = 0;

    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        // Start traversal from the program command
        if (ast.C != null) ast.C.visit(this, null);

        System.out.println("=== Program Summary ===");
        System.out.println("CharacterExpressions: " + characterCount);
        System.out.println("IntegerExpressions: " + integerCount);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
        characterCount++;
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
        integerCount++;
        return null;
    }

    @Override
    public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
        if (ast.V != null) ast.V.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
        if (ast.C1 != null) ast.C1.visit(this, null);
        if (ast.C2 != null) ast.C2.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        if (ast.C1 != null) ast.C1.visit(this, null);
        if (ast.C2 != null) ast.C2.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        if (ast.C != null) ast.C.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {
        if (ast.D != null) ast.D.visit(this, null);
        if (ast.C != null) ast.C.visit(this, null);
        return null;
    }


    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        if (ast.E1 != null) ast.E1.visit(this, null);
        if (ast.E2 != null) ast.E2.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
        if (ast.V != null) ast.V.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {
        if (ast.D != null) ast.D.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void arg) {
        if (ast.E1 != null) ast.E1.visit(this, null);
        if (ast.E2 != null) ast.E2.visit(this, null);
        if (ast.E3 != null) ast.E3.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        if (ast.D1 != null) ast.D1.visit(this, null);
        if (ast.D2 != null) ast.D2.visit(this, null);
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void arg) {
        return null;
    }


}

