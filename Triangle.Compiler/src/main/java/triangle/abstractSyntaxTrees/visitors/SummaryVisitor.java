package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;

public class SummaryVisitor
        implements ProgramVisitor<Void,Void>, CommandVisitor<Void,Void>,
        ExpressionVisitor<Void,Void>, DeclarationVisitor<Void,Void>, VnameVisitor<Void,Void>{

    private int binaryExpressionCount = 0;
    private int ifCommandsCount = 0;
    private int whileCommandsCount = 0;

    public int getBinaryExpressionCount(){

        return binaryExpressionCount;
    }

    public int getIfCommandsCount(){

        return ifCommandsCount;
    }

    public int getWhileCommandsCount(){

        return whileCommandsCount;
    }

    @Override
    public Void visitAssignCommand(AssignCommand ast, Void unused) {
        ast.E.visit(this, unused);
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void unused) {
        return null;
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void unused) {
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void unused) {
        ifCommandsCount++;
        ast.E.visit(this, unused);
        ast.C1 .visit(this, unused);
        ast.C2.visit(this, unused);
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void unused) {
        ast.D.visit(this, unused);
        ast.C.visit(this, unused);
        return null;
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void unused) {
        ast.C1.visit(this, unused);
        ast.C2.visit(this, unused);
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void unused) {
        whileCommandsCount++;
        ast.E.visit(this, unused);
        ast.C.visit(this, unused);
        return null;
    }

    @Override
    public Void visitLoopWhileCommand(LoopWhileCommand ast, Void unused) {
        return null;
    }

    @Override
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void unused) {
        ast.E.visit(this, unused);
        return null;
    }

    @Override
    public Void visitFuncDeclaration(FuncDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void unused) {
        ast.D1.visit(this, unused);
        ast.D2.visit(this, unused);
        return null;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void unused) {
        binaryExpressionCount++;
        ast.E1.visit(this, unused);
        ast.E2.visit(this, unused);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void unused) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void unused) {
        ast.E.visit(this, unused);
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void unused) {
        ast.V.visit(this, unused);
        return null;
    }

    @Override
    public Void visitConstFormalParameter(ConstFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public Void visitFuncFormalParameter(FuncFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public Void visitProcFormalParameter(ProcFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public Void visitVarFormalParameter(VarFormalParameter ast, Void unused) {
        return null;
    }

    @Override
    public Void visitProgram(Program ast, Void unused) {
        ast.C.visit(this, unused);

        return null;
    }

    @Override
    public Void visitDotVname(DotVname ast, Void unused) {
        ast.V.visit(this, unused);

        return null;
    }

    @Override
    public Void visitSimpleVname(SimpleVname ast, Void unused) {
        return null;
    }

    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void unused) {
        ast.V.visit(this, unused);
        ast.E.visit(this, unused);
        return null;
    }
}
