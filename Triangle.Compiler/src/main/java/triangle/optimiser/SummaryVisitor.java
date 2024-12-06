package triangle.optimiser;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ActualParameterSequence;
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
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.commands.RepeatCommand;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.Declaration;
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

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.visitors.*;
import java.util.Map;
import java.util.HashMap;

public class SummaryVisitor implements 
CommandVisitor<Void, Void>, 
ExpressionVisitor<Void, Void>, 
DeclarationVisitor<Void, Void>, 
ProgramVisitor<Void, Void>, 
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
public Void visitAssignCommand(AssignCommand ast, Void arg) {
    return null;
}

@Override
public Void visitCallCommand(CallCommand ast, Void arg) {
    return null;
}

@Override
public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
    return null;
}

@Override
public Void visitIfCommand(IfCommand ast, Void arg) {
    ifCommandCount++;
    ast.E.visit(this, null);
    ast.C1.visit(this, null);
    if (ast.C2 != null) {
        ast.C2.visit(this, null);
    }
    return null;
}

@Override
public Void visitLetCommand(LetCommand ast, Void arg) {
    ast.D.visit(this, arg);
    ast.C.visit(this, arg);
    return null;
}

@Override
public Void visitRepeatCommand(RepeatCommand ast, Void arg) {
    ast.E.visit(this, arg);
    ast.C.visit(this, arg);
    return null;
}

@Override
public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
    ast.C1.visit(this, arg);
    ast.C2.visit(this, arg);
    return null;
}

@Override
public Void visitWhileCommand(WhileCommand ast, Void arg) {
    whileCommandCount++;
    ast.E.visit(this, null);
    ast.C.visit(this, null);
    return null;
}

@Override
public Void visitArrayExpression(ArrayExpression ast, Void arg) {
    return null;
}

@Override
public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
    binaryExpressionCount++;
    return null;
}

@Override
public Void visitCallExpression(CallExpression ast, Void arg) {
    return null;
}

@Override
public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
    return null;
}

@Override
public Void visitEmptyExpression(EmptyExpression ast, Void arg) {
    return null;
}

@Override
public Void visitIfExpression(IfExpression ast, Void arg) {
    return null;
}

@Override
public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
    return null;
}

@Override
public Void visitLetExpression(LetExpression ast, Void arg) {
    return null;
}

@Override
public Void visitRecordExpression(RecordExpression ast, Void arg) {
    return null;
}

@Override
public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
    return null;
}

@Override
public Void visitVnameExpression(VnameExpression ast, Void arg) {
    return null;
}

@Override
public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitTypeDeclaration(TypeDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
    return null;
}

@Override
public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
    return null;
}
@Override
public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
    return null;
}

@Override
public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
    return null;
}

@Override
public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
    return null;
}

@Override
public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
    return null;
}

@Override
public Void visitProgram(Program ast, Void arg) {
    ast.C.visit(this, arg); 
    return null;
}
}