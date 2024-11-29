package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.Program;


public class SummaryVisitor implements ExpressionVisitor<Void, Void>, CommandVisitor<Void, Void>, DeclarationVisitor<Void, Void>,FormalParameterSequenceVisitor<Void, Void>,ActualParameterVisitor<Void, Void>,RecordAggregateVisitor<Void, Void>,IdentifierVisitor<Void, Void>,LiteralVisitor<Void, Void>,OperatorVisitor<Void, Void>,VnameVisitor<Void, Void>,ArrayAggregateVisitor<Void, Void>,TypeDenoterVisitor<Void, Void>,ProgramVisitor<Void, Void> {

    private int binaryExpressionCount = 0;
    private int ifCommandCount = 0;
    private int whileCommandCount = 0;

    // Methods for CommandVisitor
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
        // 
        return null;
    }

    @Override
    public Void visitIfCommand(IfCommand ast, Void arg) {
        ifCommandCount++; // Increment the count for IfCommands
        ast.C1.visit(this, null); 
        if (ast.C2 != null) {
            ast.C2.visit(this, null); // 
        }
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
        whileCommandCount++; // Increment the count for WhileCommands
        ast.E.visit(this, null); 
        ast.C.visit(this, null); // 
        return null;
    }

    // Methods for ExpressionVisitor
    @Override
    public Void visitArrayExpression(ArrayExpression ast, Void arg) {
        
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        binaryExpressionCount++; // Increment the count for BinaryExpressions
        ast.E1.visit(this, null); 
        ast.E2.visit(this, null); 
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
        ast.E2.visit(this, null); 
        ast.E3.visit(this, null); 
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        
        return null;
    }

    @Override
    public Void visitLetExpression(LetExpression ast, Void arg) {
        ast.D.visit(this, null); 
        ast.E.visit(this, null); 
        return null;
    }

    @Override
    public Void visitRecordExpression(RecordExpression ast, Void arg) {
        
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression ast, Void arg) {
        ast.E.visit(this, null); 
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        
        return null;
    }
    @Override
    public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
       
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
        
        return null;
    }

    @Override
    public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
        ast.FPS.visit(this, null); 
        ast.C.visit(this, null); 
        return null;
    }

    @Override
    public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        ast.FPS.visit(this, null); 
        ast.E.visit(this, null); 
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
    public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        
        return null;
    }

    @Override
    public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        ast.D1.visit(this, null); 
        ast.D2.visit(this, null);
        return null;
    }
    @Override
    public Void visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return null;
    }

    @Override
    public Void visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        // Visit each formal parameter sequence in the list
        ast.FPS.visit(this, null);
        return null;
    }

    @Override
    public Void visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        // Visit the single formal parameter
        ast.FP.visit(this, null);
        return null;
    }
    @Override
    public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        // You may add any logic specific to the visitor pattern here.
        // If this method is irrelevant to your summary statistics, you can leave it empty.
        return null;
    }
 // For FormalParameterVisitor interface
    @Override
    public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        // Add any specific logic for function formal parameters here, if necessary
        return null;
    }

    @Override
    public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        // Add any specific logic for variable formal parameters here, if necessary
        return null;
    }

    @Override
    public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        // Add any specific logic for constant formal parameters here, if necessary
        return null;
    }

    // For ActualParameterVisitor interface
    @Override
    public Void visitConstActualParameter(ConstActualParameter ast, Void arg) {
        // Add any specific logic for constant actual parameters here, if necessary
        return null;
    }

    @Override
    public Void visitFuncActualParameter(FuncActualParameter ast, Void arg) {
        // Add any specific logic for function actual parameters here, if necessary
        return null;
    }

    @Override
    public Void visitProcActualParameter(ProcActualParameter ast, Void arg) {
        // Add any specific logic for procedure actual parameters here, if necessary
        return null;
    }

    @Override
    public Void visitVarActualParameter(VarActualParameter ast, Void arg) {
        // Add any specific logic for variable actual parameters here, if necessary
        return null;
    }

    // For RecordAggregateVisitor interface
    @Override
    public Void visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        // Traverse the multiple record aggregate structure, if necessary
        ast.RA.visit(this, null); // Visit the next aggregate in the chain
        return null;
    }

    @Override
    public Void visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        // Process the single record aggregate, if necessary
        return null;
    }

    // For IdentifierVisitor interface
    @Override
    public Void visitIdentifier(Identifier ast, Void arg) {
        // Add logic for visiting identifiers, if required
        return null;
    }

    // For LiteralVisitor interface
    @Override
    public Void visitCharacterLiteral(CharacterLiteral ast, Void arg) {
        // Add logic for visiting character literals, if required
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral ast, Void arg) {
        // Add logic for visiting integer literals, if required
        return null;
    }

    // For OperatorVisitor interface
    @Override
    public Void visitOperator(Operator ast, Void arg) {
        // Add logic for visiting operators, if required
        return null;
    }

    // For VnameVisitor interface
    @Override
    public Void visitDotVname(DotVname ast, Void arg) {
        // Traverse the dotted vname structure, if necessary
        ast.V.visit(this, null); // Visit the next vname in the chain
        return null;
    }

    @Override
    public Void visitSimpleVname(SimpleVname ast, Void arg) {
        // Process the simple vname, if necessary
        return null;
    }

    @Override
    public Void visitSubscriptVname(SubscriptVname ast, Void arg) {
        // Traverse the subscript vname structure, if necessary
        ast.V.visit(this, null); // Visit the base vname
        ast.E.visit(this, null); // Visit the subscript expression
        return null;
    }

    // For ArrayAggregateVisitor interface
 // For ArrayAggregateVisitor interface
    @Override
    public Void visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
        // Add any specific logic for visiting multiple array aggregates, if necessary
        ast.AA.visit(this, null); // Traverse the nested array aggregates
        return null;
    }

    @Override
    public Void visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
        // Add any specific logic for visiting single array aggregates, if necessary
        return null;
    }


    // For TypeDenoterVisitor interface	
    @Override
    public Void visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
        // Visit the type of the array
        ast.T.visit(this, null);

        // Optionally, process the IntegerLiteral for the array size
        if (ast.IL != null) {
            ast.IL.visit(this, null);
        }

        return null;
    }


    @Override
    public Void visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
        // Add logic for visiting boolean type denoters, if necessary
        return null;
    }

    @Override
    public Void visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
        // Add logic for visiting character type denoters, if necessary
        return null;
    }

    @Override
    public Void visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
        // Handle error type denoters, if necessary
        return null;
    }

    @Override
    public Void visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
        // Add logic for visiting integer type denoters, if necessary
        return null;
    }

    @Override
    public Void visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        ast.FT.visit(this, null); // Traverse the fields of the record
        return null;
    }

    @Override
    public Void visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        ast.I.visit(this, null); // Visit the identifier associated with the type
        return null;
    }
    @Override
    public Void visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
        // Process the identifier and the type denoter
        ast.I.visit(this, null); // Visit the identifier
        ast.T.visit(this, null); // Visit the type denoter
        return null;
    }
    @Override
    public Void visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
        // This type might not require processing since it's abstract.
        // Implement logic here if needed.
        return null;
    }
    @Override
    public Void visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
        // Traverse the fields in the multiple field type denoter
        if (ast.T != null) {
            ast.T.visit(this, null); // Visit the first field
        }
        if (ast.FT != null) {
            ast.FT.visit(this, null); // Visit the second field
        }
        return null;
    }
    @Override
    public Void visitProgram(Program ast, Void arg) {
        // Process the Program node. For example, traverse its command:
        ast.C.visit(this, null); // Visit the command in the Program
        return null;
    }



    // Summary printing
    public void printSummary() {
        System.out.println("Summary:");
        System.out.println("Binary Expressions: " + binaryExpressionCount);
        System.out.println("If Commands: " + ifCommandCount);
        System.out.println("While Commands: " + whileCommandCount);
    }
}
