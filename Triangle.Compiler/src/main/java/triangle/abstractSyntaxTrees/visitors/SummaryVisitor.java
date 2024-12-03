package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.aggregates.ArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.RecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDenoter;
import triangle.abstractSyntaxTrees.types.FieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.LoopWhileCommand;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.SquareExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.Program;


public class SummaryVisitor implements 
        ExpressionVisitor<Void, Void>, 
        CommandVisitor<Void, Void>,
        ProgramVisitor<Void, Void>, 
        VnameVisitor<Void, Void>, 
        RecordAggregateVisitor<Void, Void>, 
        DeclarationVisitor<Void, Void>,
        LiteralVisitor<Void, Void>,
        IdentifierVisitor<Void, Void>,
        ActualParameterSequenceVisitor<Void, Void>,
        ActualParameterVisitor<Void, Void>,
        ArrayAggregateVisitor<Void, Void>,
        FieldTypeDenoterVisitor<Void, Void>,
        OperatorVisitor<Void, Void>,
        TypeDenoterVisitor<Void, Void>,
        FormalParameterSequenceVisitor<Void, Void>,
        FormalParameterVisitor<Void, Void> {

    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    public int getBinaryExpressionCount() {
        return binaryExpressionCount;
    }

    public int getIfCommandCount() {
        return ifCommandCount;
    }

    public int getWhileCommandCount() {
        return whileCommandCount;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++;
        ast.E1.visit(this, null); // Correct field access
        ast.E2.visit(this, null); // Correct field access
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++;
        ast.E.visit(this, null); // Correct field access
        ast.C1.visit(this, null); // Correct field access
        ast.C2.visit(this, null); // Correct field access
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCount++;
        ast.E.visit(this, null); // Correct field access
        ast.C.visit(this, null); // Correct field access
        return null;
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
        ast.C1.visit(this, null); // Visit the first Command field
        ast.C2.visit(this, null); // Visit the second Command field
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        ast.D.visit(this, null); // Visit the Declaration field
        ast.C.visit(this, null); // Visit the Command field
        return null;
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.APS.visit(this, null); // Visit the ActualParameterSequence field
        return null;
    }

    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.V.visit(this, null); // Visit the Vname field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitSquareExpression(SquareExpression ast, Void arg) {
        ast.expression.visit(this, null); // Visit the expression field
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.E.visit(this, null); // Visit the expression field
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        ast.V.visit(this, null); // Visit the Vname field
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        ast.RA.visit(this, null); // Visit the RecordAggregate field
        return null;
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void arg) {
        ast.D.visit(this, null); // Visit the Declaration field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        ast.IL.visit(this, null); // Visit the IntegerLiteral field
        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression ast, Void arg) {
        ast.E1.visit(this, null); // Visit the first expression field
        ast.E2.visit(this, null); // Visit the second expression field
        ast.E3.visit(this, null); // Visit the third expression field
        return null;
    }

    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        ast.CL.visit(this, null); // Visit the CharacterLiteral field
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.APS.visit(this, null); // Visit the ActualParameterSequence field
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        ast.AA.visit(this, null); // Visit the ArrayAggregate field
        return null;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclaration ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitDotVname(DotVname ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.V.visit(this, null); // Visit the Vname field
        return null;
    }
    
    @Override
    public Void visitSimpleVname(SimpleVname ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        return null;
    }

    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void arg) {
        ast.V.visit(this, null); // Visit the Vname field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.E.visit(this, null); // Visit the Expression field
        ast.RA.visit(this, null); // Visit the RecordAggregate field
        return null;
    }

    @Override
    public Void visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        ast.O.visit(this, null); // Visit the Operator field
        ast.ARG1.visit(this, null); // Visit the Argument1 field
        ast.ARG2.visit(this, null); // Visit the Argument2 field
        ast.RES.visit(this, null); // Visit the Result field
        return null;
    }

    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.FPS.visit(this, null); // Visit the FormalParameterSequence field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.FPS.visit(this, null); // Visit the FormalParameterSequence field
        ast.C.visit(this, null); // Visit the Command field
        return null;
    }

    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this, null); // Visit the first Declaration field
        ast.D2.visit(this, null); // Visit the second Declaration field
        return null;
    }

    @Override
    public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
        ast.O.visit(this, null); // Visit the Operator field
        ast.ARG.visit(this, null); // Visit the Argument field
        ast.RES.visit(this, null); // Visit the Result field
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        return null;
    }

    @Override
    public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitCharacterLiteral(CharacterLiteral ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitIdentifier(Identifier ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this, null); // Visit the ActualParameter field
        return null;
    }

    @Override
    public Void visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
        ast.AP.visit(this, null); // Visit the ActualParameter field
        ast.APS.visit(this, null); // Visit the ActualParameterSequence field
        return null;
    }

    @Override
    public Void visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
        ast.E.visit(this, null); // Visit the Expression field
        ast.AA.visit(this, null); // Visit the ArrayAggregate field
        return null;
    }

    @Override
    public Void visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        ast.T.visit(this, null); // Visit the TypeDenoter field
        ast.FT.visit(this, null); // Visit the FieldTypeDenoter field
        return null;
    }

    @Override
    public Void visitOperator(Operator ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        ast.FT.visit(this, null); // Visit the FieldTypeDenoter field
        return null;
    }

    @Override
    public Void visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        return null;
    }

    @Override
    public Void visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
        ast.T.visit(this, null); // Visit the TypeDenoter field
        return null;
    }

    @Override
    public Void visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }

    @Override
    public Void visitConstActualParameter(ConstActualParameter ast, Void arg) {
        ast.E.visit(this, null); // Visit the Expression field
        return null;
    }

    @Override
    public Void visitFuncActualParameter(FuncActualParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        return null;
    }

    @Override
    public Void visitProcActualParameter(ProcActualParameter ast, Void arg) {
        ast.I.visit(this, null); // Visit the Identifier field
        return null;
    }

    @Override
    public Void visitVarActualParameter(VarActualParameter ast, Void arg) {
        ast.V.visit(this, null); // Visit the Vname field
        return null;
    }

    @Override
    public Void visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this, null); // Visit the FormalParameter field
        return null;
    }

    @Override
    public Void visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        ast.FP.visit(this, null); // Visit the FormalParameter field
        ast.FPS.visit(this, null); // Visit the FormalParameterSequence field
        return null;
    }
    
    @Override
    public Void visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        // Simply return null as there is no further traversal needed
        return null;
    }
    
    @Override public Void visitLoopWhileCommand(LoopWhileCommand ast, Void arg) { 
    	ast.C1.visit(this, arg); 
    	ast.E.visit(this, arg); 
    	ast.C2.visit(this, arg); 
    	return null;
    }

}






