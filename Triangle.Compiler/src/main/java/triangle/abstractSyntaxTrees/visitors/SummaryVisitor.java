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
implements ProgramVisitor<Void, Void>,
        CommandVisitor<Void, Void>,
        ExpressionVisitor<Void, Void>,
        DeclarationVisitor<Void, Void>,
        VnameVisitor<Void, Void>
{
    //counters for constructs
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;
    private int binaryExpressionCount = 0;


    //visit the root of the AST
    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this,arg);
        return null;
    }

    //visit if command, increment counter and visit its components
    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++;
        ast.E.visit(this, arg); //visit condition
        ast.C1.visit(this, arg);//visit then
        ast.C2.visit(this, arg);//visit else
        return null;


    }

    //visit while command, increment the count and visit its components
    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCount++;
        ast.E.visit(this, arg); //visit condition
        ast.C.visit(this, arg); //visit body
        return null;

    }

    //visit loop-while command
    @Override
    public Void visitLoopWhileCommand(LoopWhileCommand ast, Void unused) {
        return null;
    }

    //visit sequential commands - series of commands that execute in sequence
    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
        ast.C1.visit(this, arg); //visit
        ast.C2.visit(this, arg); //visit next command
        return null;
    }


    //visit let command, visiting its declaration and body.
    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        ast.D.visit(this, arg); // Visit the declaration
        ast.C.visit(this, arg); // Visit the body command
        return null;
    }

    //visit an assignment command
    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.E.visit(this, arg); // Visit the expression being assigned
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
    public Void visitArrayExpression(ArrayExpression ast, Void unused) {
        return null;
    }

    //visit a binary expression, increment the count and visit its operands
    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++;
        ast.E1.visit(this, arg); //visit left operand
        ast.E2.visit(this, arg); //visit right operand
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


    //visit a unary expression and its operands.
    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.E.visit(this, arg); // Visit the operand
        return null;
    }

    //visit a vname expression and its variable name
    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        ast.V.visit(this, arg); // Visit the variable name
        return null;
    }

    // visit sequential declaration and its components
    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this, arg); // Visit the first declaration
        ast.D2.visit(this, arg); // Visit the second declaration
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
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    //visit a constant declaration and its assigned expression
    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
        ast.E.visit(this, arg); // Visit the assigned expression
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
    public Void visitSimpleVname(SimpleVname ast, Void arg) {
        return null; // No further nodes to visit
    }

    //visit a dot name and its parent object
    @Override
    public Void visitDotVname(DotVname ast, Void arg) {
        ast.V.visit(this, arg); // Visit the parent object
        return null;
    }

    //visit subscript vname and its components
    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void arg) {
        ast.E.visit(this, arg); // Visit the subscript expression
        ast.V.visit(this, arg); // Visit the base variable
        return null;
    }

    // Print the summary of counts
    public void printSummary() {
        System.out.println("If Commands: " + ifCommandCount);
        System.out.println("While Commands: " + whileCommandCount);
        System.out.println("Binary Expressions: " + binaryExpressionCount);
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

