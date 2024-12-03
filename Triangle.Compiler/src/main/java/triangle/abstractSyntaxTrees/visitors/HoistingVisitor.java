package triangle.optimiser;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.actuals.*;  // Import actual parameter sequences
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;  // Import declarations
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.*;  // Import formal parameter sequences
import triangle.abstractSyntaxTrees.terminals.*;
import triangle.abstractSyntaxTrees.types.*;
import triangle.abstractSyntaxTrees.vnames.*;
import triangle.abstractSyntaxTrees.visitors.*;
import triangle.abstractSyntaxTrees.aggregates.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HoistingVisitor implements CommandVisitor<Void, Void>, ExpressionVisitor<Void, Void>, VnameVisitor<Void, Void>, ActualParameterSequenceVisitor<Void, Void>, DeclarationVisitor<Void, Void>, FormalParameterSequenceVisitor<Void, Void>, FormalParameterVisitor<Void, Void>, ProgramVisitor<Void, Void> {
    private Set<String> updatedVariables = new HashSet<>();
    private Map<Expression, String> hoistedExpressions = new HashMap<>();
    private int tempVarCounter = 0;

    public HoistingVisitor(Set<String> updatedVariables) {
        this.updatedVariables = updatedVariables;
    }

    
    @Override
    public Void visitProgram(Program ast, Void arg) {
        ast.C.visit(this, arg); // Visit the command
        return null;
    }

    
    @Override
    public Void visitAssignCommand(AssignCommand ast, Void arg) {
        ast.V.visit(this, null); // Visit the variable being assigned to
        ast.E.visit(this, null); // Visit the expression on the right-hand side
        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Void arg) {
        ast.APS.visit(this, null); // Correcting the visitor method for ActualParameterSequence
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
        // Visit the body of the while loop to identify updated variables
        ast.C.visit(this, null);
        return null;
    }

    @Override
    public Void visitLoopWhileCommand(LoopWhileCommand ast, Void arg) {
        // Implement the visit logic for LoopWhileCommand
        ast.C1.visit(this, null);
        ast.C2.visit(this, null);
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Void arg) {
        ast.D.visit(this, null); // Visit the declaration part
        ast.C.visit(this, null); // Visit the command part
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
    public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        ast.V.visit(this, null);
        return null;
    }


    @Override
    public Void visitSimpleVname(SimpleVname ast, Void arg) {
        updatedVariables.add(ast.I.spelling);
        return null;
    }
    
    @Override
    public Void visitDotVname(DotVname ast, Void arg) {
        updatedVariables.add(ast.I.spelling); // Add the identifier's spelling to the set of updated variables
        ast.V.visit(this, arg); // Visit the parent Vname
        return null;
    }


    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void arg) {
        ast.V.visit(this, null);
        ast.E.visit(this, null);
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitSquareExpression(SquareExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void arg) {
        ast.D.visit(this, null); // Visit the declaration part
        ast.E.visit(this, null); // Visit the expression part
        return null;
    }

    @Override
    public Void visitIfExpression(IfExpression ast, Void arg) {
        ast.E1.visit(this, null); // Visit the condition
        ast.E2.visit(this, null); // Visit the then expression
        ast.E3.visit(this, null); // Visit the else expression
        return null;
    }

    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        ast.E1.visit(this, null);
        ast.E2.visit(this, null);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        ast.APS.visit(this, null);
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
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.E.visit(this, null);
        return null;
    }

    // Implementing the required methods for DeclarationVisitor interface
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


    // Second pass: Hoist invariant expressions
    private void hoistExpression(Expression expr) {
        if (!hoistedExpressions.containsKey(expr)) {
            String tempVarName = "tmp" + (++tempVarCounter);
            hoistedExpressions.put(expr, tempVarName);
        }
    }

    private boolean canBeHoisted(Expression expr) {
        // Check if the expression can be hoisted
        if (expr instanceof VnameExpression) {
            VnameExpression vnameExpr = (VnameExpression) expr;
            return !updatedVariables.contains(vnameExpr.V.toString());
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression binaryExpr = (BinaryExpression) expr;
            return canBeHoisted(binaryExpr.E1) && canBeHoisted(binaryExpr.E2);
        }
        // Add more cases as needed for other expression types
        return true;
    }

    private void modifyLoopBody(Command ast) {
        if (ast instanceof AssignCommand) {
            AssignCommand assignCommand = (AssignCommand) ast;
            if (!updatedVariables.contains(assignCommand.V.toString()) && canBeHoisted(assignCommand.E)) {
                hoistExpression(assignCommand.E);
            }
        } else if (ast instanceof SequentialCommand) {
            SequentialCommand sequentialCommand = (SequentialCommand) ast;
            modifyLoopBody(sequentialCommand.C1);
            modifyLoopBody(sequentialCommand.C2);
        }
        
    }

   
    public Void visitVname(Vname ast, Void arg) {
        // Visit the Vname
        
        if (ast instanceof SimpleVname) {
            visitSimpleVname((SimpleVname) ast, arg);
        } else if (ast instanceof SubscriptVname) {
            visitSubscriptVname((SubscriptVname) ast, arg);
        }
        return null;
    }

    // Implementing required methods for ActualParameterSequenceVisitor interface
    @Override
    public Void visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
        ast.APS.visit(this, arg);
        return null;
    }

    @Override
    public Void visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
        return null;
    }

    // Implementing required methods for FormalParameterSequenceVisitor interface
    @Override
    public Void visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        return null;
    }

    // Implementing required methods for FormalParameterVisitor interface
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

