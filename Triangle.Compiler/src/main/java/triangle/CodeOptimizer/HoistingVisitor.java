package triangle.optimiser;

import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;

import java.util.HashSet;
import java.util.Set;

public class HoistingVisitor implements CommandVisitor<Void, Void>, ExpressionVisitor<Void, Void> {
    private Set<String> updatedIdentifiers = new HashSet<>();
    private LetCommand hoistedLet = null;

    @Override
    public Void visitWhileCommand(WhileCommand ast, Void arg) {
        // Reset the hoisted let for each while command
        hoistedLet = null;
        updatedIdentifiers.clear();

        // First pass: collect updated identifiers
        collectUpdatedIdentifiers(ast.C);

        // Second pass: hoist invariant expressions
        hoistInvariants(ast);

        return null;
    }

    private void collectUpdatedIdentifiers(Command command) {
        // Traverse the command to find updated identifiers
        command.visit(this, null);
    }

    private void hoistInvariants(WhileCommand ast) {
        // Traverse the loop body and identify invariant expressions
        for (Expression expr : ast.getExpressions()) {
            if (isInvariant(expr)) {
                // Create a temporary variable for the invariant expression
                String tempVar = "tmp"; // You may want to generate a unique name
                hoistedLet = new LetCommand(tempVar, expr);
                // Replace occurrences of the invariant expression with the temporary variable
                replaceInvariants(ast, tempVar);
                break; // Only hoist one invariant for simplicity
            }
        }

        // If we found an invariant, add the hoisted let above the while loop
        if (hoistedLet != null) {
            // Add hoistedLet to the AST (you may need to adjust this part based on your AST structure)
            ast.addBefore(hoistedLet);
        }
    }

    private boolean isInvariant(Expression expr) {
        // Check if the expression contains any updated identifiers
        for (Vname vname : expr.getVnames()) {
            if (updatedIdentifiers.contains(vname.getName())) {
                return false; // Not invariant
            }
        }
        return true; // Invariant
    }

    private void replaceInvariants(WhileCommand ast, String tempVar) {
        // Replace occurrences of the invariant expression with the temporary variable
        ast.replaceExpressionWithVname(tempVar);
    }

    // Implement other visit methods for expressions
    @Override
    public Void visitBinaryExpression(BinaryExpression ast, Void arg) {
        ast.E1.visit(this, arg);
        ast.E2.visit(this, arg);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression ast, Void arg) {
        // Handle call expressions if needed
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        return null;
    }

    // Add other necessary visit methods for different expression types
}