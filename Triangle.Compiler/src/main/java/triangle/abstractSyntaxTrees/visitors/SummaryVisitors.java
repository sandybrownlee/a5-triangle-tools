package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;

public class SummaryVisitors implements ProgramVisitor<Void, AbstractSyntaxTree>,
        CommandVisitor<Void, AbstractSyntaxTree>,
        ExpressionVisitor<Void, AbstractSyntaxTree>,
        OperatorVisitor<Void, AbstractSyntaxTree>,
        DeclarationVisitor<Void, AbstractSyntaxTree>{

    //Counters
    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    //Constructor
    public SummaryVisitors() {
        this.binaryExpressionCount = 0;
        this.ifCommandCount = 0;
        this.whileCommandCount = 0;
    }

    //Print summary
    public void getSummary() {
        System.out.println("Summary of program:");
        System.out.println("Binary Expressions: " + binaryExpressionCount);
        System.out.println("If Commands: " + ifCommandCount);
        System.out.println("While Commands: " + whileCommandCount);
    }

    //Visit BinaryExpression and increment its count
    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++;
        if (ast.E1 != null) {
            ast.E1.visit(this, arg);
        }
        if (ast.E2 != null) {
            ast.E2.visit(this, arg);
        }
        if (ast.O != null) {
            ast.O.visit(this, arg);
        }
        return null;
    }

    //Visit IfCommand and increment its count
    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++;
        if (ast.E != null) {
            ast.E.visit(this, arg);
        }
        if (ast.C1 != null) {
            ast.C1.visit(this, arg);
        }
        if (ast.C2 != null) {
            ast.C2.visit(this, arg);
        }
        return null;
    }

    //Visit WhileCommand and increment its count
    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        whileCommandCount++;
        if (ast.E != null) {
            ast.E.visit(this, arg);
        }
        if (ast.C != null) {
            ast.C.visit(this, arg);
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitWeirdWhileCommand(WeirdWhileCommand command, Void unused) {
        return null;
    }

    //Visit SequentialCommand to ensure all child commands are visited
    @Override
    public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
        if (ast.C1 != null) {
            ast.C1.visit(this, arg);
        }
        if (ast.C2 != null) {
            ast.C2.visit(this, arg);
        }
        return null;
    }

    //Visit LetCommand to visit its declaration and command
    @Override
    public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {
        if (ast.D != null) {
            ast.D.visit(this, arg);
        }
        if (ast.C != null) {
            ast.C.visit(this, arg);
        }
        return null;
    }

    //Visit Program to start the traversal
    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        if (ast.C != null) {
            ast.C.visit(this, arg); // Traverse child commands
        }
        return null;
    }

    @Override
    public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
        if (ast.E != null) {
            ast.E.visit(this, arg); //Visit the expression being assigned
        }
        return null;
    }

    //-----UNCHANGED METHODS------

    @Override
    public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void unused) {
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
    public AbstractSyntaxTree visitOperator(Operator ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void unused) {
        return null;
    }

    @Override
    public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void unused) {
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
    public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void unused) {
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
    public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void unused) {
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
}
