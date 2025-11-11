package triangle.optimiser;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.commands.WhileDoCommand;
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
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;

public class StatisticsCollector implements
        ProgramVisitor<Void, Void>,
        ExpressionVisitor<Void, Void>,
        CommandVisitor<Void, Void>,
        DeclarationVisitor<Void, Void> {

    private int charCount = 0;
    private int intCount = 0;

    public int getCharacterCount() {
        return charCount;
    }

    public int getIntegerCount() {
        return intCount;
    }

    @Override
    public Void visitProgram(Program ast, Void arg) {
        if (ast.C != null)
            ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        intCount++;
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        charCount++;
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.E.visit(this, null);
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
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void arg) {
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
    public Void visitWhileDoCommand(WhileDoCommand ast, Void arg) {
        ast.command1.visit(this, null);
        ast.expression.visit(this, null);
        ast.command2.visit(this, null);
        return null;
    }

    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
        return null;
    }

    @Override
    public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
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
        return null;
    }

    @Override
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        return null;
    }

    @Override
    public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
        return null;
    }

     @Override
    public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        return null;
    }

    @Override
    public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        return null;
    }
}
