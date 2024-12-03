package triangle.optimiser;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.vnames.*;
import triangle.abstractSyntaxTrees.visitors.*;
import triangle.abstractSyntaxTrees.aggregates.*;

import java.util.Set;

public class InvariantIdentifierVisitor implements CommandVisitor<Set<String>, Void>, ExpressionVisitor<Set<String>, Void>, VnameVisitor<Set<String>, Void>, ProgramVisitor<Set<String>, Void> {

    @Override
    public Void visitAssignCommand(AssignCommand ast, Set<String> updatedVariables) {
        ast.V.visit(this, updatedVariables); // Visit the variable being assigned to
        ast.E.visit(this, updatedVariables); // Visit the expression on the right-hand side

        if (ast.V instanceof SimpleVname) {
            SimpleVname simpleVname = (SimpleVname) ast.V;
            updatedVariables.add(simpleVname.I.spelling);
        } else if (ast.V instanceof DotVname) {
            DotVname dotVname = (DotVname) ast.V;
            updatedVariables.add(dotVname.I.spelling);
        } else if (ast.V instanceof SubscriptVname) {
            SubscriptVname subscriptVname = (SubscriptVname) ast.V;
            // Handle subscripted vname if necessary
        }

        return null;
    }

    @Override
    public Void visitCallCommand(CallCommand ast, Set<String> updatedVariables) {
        // Typically, Identifier doesn't need a visit method like other AST nodes
        updatedVariables.add(ast.I.spelling); // Add the identifier's spelling to updatedVariables
        return null;
    }


    @Override
    public Void visitEmptyCommand(EmptyCommand ast, Set<String> updatedVariables) {
        return null; // Simply return as there is no action
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression (condition)
        ast.C1.visit(this, updatedVariables); // Visit the command for the "then" part
        ast.C2.visit(this, updatedVariables); // Visit the command for the "else" part
        return null;
    }

    @Override
    public Void visitLetCommand(LetCommand ast, Set<String> updatedVariables) {
        ast.C.visit(this, updatedVariables); // Visit the command
        return null;
    }

    @Override
    public Void visitSequentialCommand(SequentialCommand ast, Set<String> updatedVariables) {
        ast.C1.visit(this, updatedVariables); // Visit the first command in sequence
        ast.C2.visit(this, updatedVariables); // Visit the second command in sequence
        return null;
    }

    @Override
    public Void visitWhileCommand(WhileCommand ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression (condition)
        ast.C.visit(this, updatedVariables); // Visit the command (body of the loop)
        return null;
    }

    @Override
    public Void visitLoopWhileCommand(LoopWhileCommand ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression (condition)
        ast.C1.visit(this, updatedVariables); // Visit the command for the "do" part
        ast.C2.visit(this, updatedVariables); // Visit the command for the "loop" part
        return null;
    }

    // Implementing required methods for VnameVisitor interface
    @Override
    public Void visitDotVname(DotVname ast, Set<String> updatedVariables) {
        updatedVariables.add(ast.I.spelling);
        return null;
    }

    @Override
    public Void visitSimpleVname(SimpleVname ast, Set<String> updatedVariables) {
        updatedVariables.add(ast.I.spelling);
        return null;
    }

    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Set<String> updatedVariables) {
        ast.V.visit(this, updatedVariables); // Visit the variable part
        ast.E.visit(this, updatedVariables); // Visit the subscript expression
        return null;
    }

    // Implementing required methods for ExpressionVisitor interface
    @Override
    public Void visitVnameExpression(VnameExpression ast, Set<String> updatedVariables) {
        ast.V.visit(this, updatedVariables); // Visit the variable
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Set<String> updatedVariables) {
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Set<String> updatedVariables) {
        ast.E1.visit(this, updatedVariables); // Visit the left expression
        ast.E2.visit(this, updatedVariables); // Visit the right expression
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Set<String> updatedVariables) {
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Set<String> updatedVariables) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the operand expression
        return null;
    }


    @Override
    public Void visitArrayExpression(ArrayExpression ast, Set<String> updatedVariables) {
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Set<String> updatedVariables) {
        return null;
    }
    
    @Override
    public Void visitSquareExpression(SquareExpression ast, Set<String> updatedVariables) {
        return null;
    }

    @Override 
    public Void visitLetExpression(LetExpression ast, Set<String> updatedVariables) { 
    	ast.E.visit(this, updatedVariables); // Visit the expression 
    	return null;
    }
    
    @Override
    public Void visitEmptyExpression(EmptyExpression ast, Set<String> updatedVariables) {
        return null;
    }

    
    @Override
    public Void visitIfExpression(IfExpression ast, Set<String> updatedVariables) {
        ast.E1.visit(this, updatedVariables); // Visit the condition
        ast.E2.visit(this, updatedVariables); // Visit the then-expression
        ast.E3.visit(this, updatedVariables); // Visit the else-expression
        return null;
    }


    public Void visitSingleRecordAggregate(SingleRecordAggregate ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression
        return null;
    }

    public Void visitMultipleRecordAggregate(MultipleRecordAggregate ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression
        return null;
    }

    public Void visitSingleArrayAggregate(SingleArrayAggregate ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression
        return null;
    }

    public Void visitMultipleArrayAggregate(MultipleArrayAggregate ast, Set<String> updatedVariables) {
        ast.E.visit(this, updatedVariables); // Visit the expression
        return null;
    }

    // Implementing required methods for ProgramVisitor interface
    @Override
    public Void visitProgram(Program ast, Set<String> arg) {
        ast.C.visit(this, arg);
        return null;
    }
}

