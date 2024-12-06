package triangle.optimiser;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;

 /*
   this class will generate statistics: counting how many BinaryExpressions, IfCommands
   and WhileCommands there are in a given triangle program
   this class will also traverse the AST and count the nodes
    */

public class SummaryVisitor
        implements ProgramVisitor<Void, Void>, CommandVisitor<Void, Void>, ExpressionVisitor<Void, Void>,
        DeclarationVisitor<Void, Void>{

    private int binaryExpressionCounter = 0;
    private int ifCommandCounter = 0;
    private int whileCommandCounter = 0;

    // these methods are created to return the counts later used in the compiler
    public int getBinaryExpressionCount() {
        return binaryExpressionCounter;
    }

    public int getIfCommandCount() {
        return ifCommandCounter;
    }

    public int getWhileCommandCount() {
        return whileCommandCounter;
    }

    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this, arg);
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCounter++;
        ast.E1.visit(this, arg);
        ast.E2.visit(this, arg);
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCounter++;
        ast.E.visit(this, arg);
        ast.C1.visit(this, arg);
        ast.C2.visit(this, arg);
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCounter++;
        ast.E.visit(this, arg);
        ast.C.visit(this, arg);
        return null;
    }

    // empty implementations for other methods of CommandVisitor
    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.E.visit(this, arg);
        return null;
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
        ast.C1.visit(this, arg);
        ast.C2.visit(this, arg);
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        ast.D.visit(this, arg);
        ast.C.visit(this, arg);
        return null;
    }

    @Override
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
        ast.E.visit(this, arg);
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
        return null;
    }

    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this, arg);
        ast.D2.visit(this, arg);
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
    public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        ast.E.visit(this, arg);
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
        ast.C.visit(this, arg);
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void unused) {
        return null;
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    // empty implementations for other methods of ExpressionVisitor
    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
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
}
