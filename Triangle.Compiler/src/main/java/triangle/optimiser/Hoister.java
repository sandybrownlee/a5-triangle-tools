package triangle.optimiser;

import java.util.HashSet;
import java.util.Set;

import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.syntacticAnalyzer.SourcePosition;

/**
 * Minimal loop-invariant hoister for while loops (Task 8).
 *
 * Strategy (non-mutating with respect to final fields):
 * - Recursively build a transformed Command tree.
 * - On encountering a WhileCommand:
 *   - Transform the body first.
 *   - Collect identifiers assigned within the (transformed) body.
 *   - Find a hoistable assignment: an AssignCommand whose RHS is invariant
 *     (no referenced identifiers are assigned in the loop body) and is a simple BinaryExpression tree.
 *   - Hoist by creating a const declaration 'const tmp ~ RHS' above the while,
 *     and replace the original RHS in that specific assignment with 'tmp'.
 *   - Return LetCommand(const tmp ~ RHS, WhileCommand(E, transformedBody)).
 *
 * Notes:
 * - Conservative: handles a single invariant per while and simple expression forms.
 * - AssignCommand.E is mutable (non-final) so replacement is done in place for that node.
 */
public final class Hoister {

    public static Command transform(Command root) {
        return transformCommand(root);
    }

    private static Command transformCommand(Command cmd) {
        if (cmd == null) return null;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            Command c1 = transformCommand(sc.C1);
            Command c2 = transformCommand(sc.C2);
            boolean changed = (c1 != sc.C1) || (c2 != sc.C2);
            return changed ? new SequentialCommand(c1, c2, sc.getPosition()) : sc;

        } else if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            Command inner = transformCommand(lc.C);
            boolean changed = (inner != lc.C);
            return changed ? new LetCommand(lc.D, inner, lc.getPosition()) : lc;

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            // First transform inside body
            Command transformedBody = transformCommand(wc.C);

            // Collect identifiers assigned in the loop body
            Set<String> assigned = new HashSet<>();
            collectAssignedIdentifiers(transformedBody, assigned);

            // Build reserved set (avoid temp-name collisions)
            Set<String> reserved = new HashSet<>(assigned);
            collectReferencedIdentifiers(transformedBody, reserved);
            collectIdentifiersInExpression(wc.E, reserved);

            // Try to locate a hoistable assignment in the (transformed) body
            AssignCommand targetAssign = findHoistableAssignment(transformedBody, assigned);

            if (targetAssign != null && isInvariant(targetAssign.E, assigned)) {
                // Create const tmpX ~ RHS with a collision-free temp
                SourcePosition pos = targetAssign.getPosition();
                Identifier tmpId = new Identifier(freshTempName(reserved), pos);
                Expression rhsClone = cloneExpression(targetAssign.E);
                ConstDeclaration constDecl = new ConstDeclaration(tmpId, rhsClone, pos);

                // Replace RHS of the located assignment with reference to tmpX
                SimpleVname tmpV = new SimpleVname(tmpId, pos);
                VnameExpression tmpExpr = new VnameExpression(tmpV, pos);
                targetAssign.E = tmpExpr;

                // Rebuild While with transformed body
                WhileCommand liftedWhile = new WhileCommand(wc.E, transformedBody, wc.getPosition());
                return new LetCommand(constDecl, liftedWhile, wc.getPosition());
            }

            // No hoistable target; rebuild While if body changed
            boolean changed = (transformedBody != wc.C);
            return changed ? new WhileCommand(wc.E, transformedBody, wc.getPosition()) : wc;

        } else if (cmd instanceof EmptyCommand || cmd instanceof AssignCommand) {
            // No structural change for these nodes here
            return cmd;

        } else {
            // Unknown/other command types
            return cmd;
        }
    }

    private static void collectAssignedIdentifiers(Command cmd, Set<String> out) {
        if (cmd == null) return;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            collectAssignedIdentifiers(sc.C1, out);
            collectAssignedIdentifiers(sc.C2, out);

        } else if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            collectAssignedIdentifiers(lc.C, out);

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            collectAssignedIdentifiers(wc.C, out);

        } else if (cmd instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) cmd;
            String base = extractBaseIdentifier(ac.V);
            if (base != null) out.add(base);
        }
    }

    private static AssignCommand findHoistableAssignment(Command cmd, Set<String> assigned) {
        if (cmd == null) return null;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            AssignCommand left = findHoistableAssignment(sc.C1, assigned);
            if (left != null) return left;
            return findHoistableAssignment(sc.C2, assigned);

        } else if (cmd instanceof LetCommand) {
            return findHoistableAssignment(((LetCommand) cmd).C, assigned);

        } else if (cmd instanceof WhileCommand) {
            return findHoistableAssignment(((WhileCommand) cmd).C, assigned);

        } else if (cmd instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) cmd;
            if (ac.E instanceof BinaryExpression && isInvariant(ac.E, assigned)) {
                return ac;
            }
        }
        return null;
    }

    private static boolean isInvariant(Expression expr, Set<String> assigned) {
        if (expr instanceof VnameExpression) {
            String id = extractBaseIdentifier(((VnameExpression) expr).V);
            return id == null || !assigned.contains(id);

        } else if (expr instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expr;
            return isInvariant(be.E1, assigned) && isInvariant(be.E2, assigned);

        } else if (expr instanceof IntegerExpression) {
            return true;
        }
        // Other expression types not handled => conservative false
        return false;
    }

    private static String extractBaseIdentifier(Vname v) {
        if (v instanceof SimpleVname) {
            return ((SimpleVname) v).I.spelling;
        }
        // Dot/Subscript not handled conservatively
        return null;
    }

    private static void collectIdentifiersInExpression(Expression expr, Set<String> out) {
        if (expr == null) return;

        if (expr instanceof VnameExpression) {
            String id = extractBaseIdentifier(((VnameExpression) expr).V);
            if (id != null) out.add(id);

        } else if (expr instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expr;
            collectIdentifiersInExpression(be.E1, out);
            collectIdentifiersInExpression(be.E2, out);

        } else if (expr instanceof IntegerExpression) {
            // literals contribute no identifiers
        } else {
            // Extend here if you support more expression node kinds
        }
    }

    private static void collectReferencedIdentifiers(Command cmd, Set<String> out) {
        if (cmd == null) return;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            collectReferencedIdentifiers(sc.C1, out);
            collectReferencedIdentifiers(sc.C2, out);

        } else if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            collectReferencedIdentifiers(lc.C, out);

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            collectIdentifiersInExpression(wc.E, out);
            collectReferencedIdentifiers(wc.C, out);

        } else if (cmd instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) cmd;
            // LHS base id is an lvalue; reserve it as well
            String base = extractBaseIdentifier(ac.V);
            if (base != null) out.add(base);
            // Collect from RHS
            collectIdentifiersInExpression(ac.E, out);
        }
    }

    private static String freshTempName(Set<String> reserved) {
        String base = "tmp";
        if (!reserved.contains(base)) return base;
        int i = 1;
        while (true) {
            String candidate = base + "$" + i;
            if (!reserved.contains(candidate)) return candidate;
            i++;
        }
    }

    private static Expression cloneExpression(Expression e) {
        SourcePosition pos = e.getPosition();
        if (e instanceof IntegerExpression) {
            IntegerExpression ie = (IntegerExpression) e;
            IntegerLiteral il = new IntegerLiteral(ie.IL.spelling, pos);
            return new IntegerExpression(il, pos);

        } else if (e instanceof VnameExpression) {
            VnameExpression ve = (VnameExpression) e;
            String base = extractBaseIdentifier(ve.V);
            if (base != null) {
                Identifier id = new Identifier(base, pos);
                SimpleVname sv = new SimpleVname(id, pos);
                return new VnameExpression(sv, pos);
            }
            return ve;

        } else if (e instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) e;
            Operator op = new Operator(be.O.spelling, pos);
            Expression e1 = cloneExpression(be.E1);
            Expression e2 = cloneExpression(be.E2);
            return new BinaryExpression(e1, op, e2, pos);
        }
        return e;
    }
}