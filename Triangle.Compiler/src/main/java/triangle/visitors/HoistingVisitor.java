package triangle.visitors;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.terminals.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.vnames.*;
import java.util.HashSet;
import java.util.Set;

public class HoistingVisitor extends Visitor {

    private final Set<String> modifiedVariables = new HashSet<>();

    @Override
    public Object visitWhileCommand(WhileCommand command, Object arg) {
        // Identify modified variables inside the loop
        command.C.visit(this, null);

        // Identify invariant expressions (not dependent on modified variables)
        if (command.C instanceof AssignCommand) {
            AssignCommand assignCmd = (AssignCommand) command.C;

            if (isInvariant(assignCmd.E)) {
                // Hoist the invariant expression outside the loop
                ConstDeclaration hoistedConst = new ConstDeclaration(
                        new Identifier("tmp"),
                        assignCmd.E,
                        command.position
                );

                // Replace the original expression with the hoisted constant
                assignCmd.E = new VnameExpression(
                        new SimpleVname(new Identifier("tmp"), command.position),
                        command.position
                );

                System.out.println("Hoisted: " + assignCmd.V + " := " + assignCmd.E);
                return new LetCommand(hoistedConst, command, command.position);
            }
        }
        return command;
    }

    @Override
    public Object visitAssignCommand(AssignCommand command, Object arg) {
        if (command.V instanceof SimpleVname) {
            modifiedVariables.add(((SimpleVname) command.V).I.spelling);
        }
        return null;
    }

    private boolean isInvariant(Expression expr) {
        if (expr instanceof IntegerExpression || expr instanceof CharacterExpression) {
            return true; // Constants are always invariant
        }
        if (expr instanceof VnameExpression) {
            Vname vname = ((VnameExpression) expr).V;
            if (vname instanceof SimpleVname) {
                return !modifiedVariables.contains(((SimpleVname) vname).I.spelling);
            }
        }
        return false; // Assume non-simple expressions are not invariant
    }
}