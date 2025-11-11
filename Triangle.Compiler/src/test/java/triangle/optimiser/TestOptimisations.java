package triangle.optimiser;

import org.junit.Test;
import static org.junit.Assert.*;

import triangle.ErrorReporter;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.Parser;
import triangle.contextualAnalyzer.Checker;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;

import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;

import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.Vname;

/**
 * Optimisation-focused tests to strengthen coverage for Tasks 6–8:
 * - StatsVisitor counts on a real program (booleans-to-fold)
 * - Constant folding removes literal comparisons
 * - Hoister introduces a Let with a hoisted invariant above while
 */
public class TestOptimisations {

    @Test
    public void testStatsVisitorOnBooleansProgram() {
        SourceFile source = SourceFile.fromResource("/booleans-to-fold.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        Program p = parser.parseProgram();
        assertEquals("Syntactic errors present in booleans-to-fold.tri", 0, reporter.getNumErrors());

        // StatsVisitor should traverse the whole AST and count integer and character expressions.
        StatsVisitor stats = new StatsVisitor();
        p.visit(stats);

        int ints = stats.getIntegerCount();
        int chars = stats.getCharacterCount();

        // Sanity bounds for this example program:
        // - It contains multiple integer literals in comparisons and multiple character literals in put('x')
        assertTrue("Expected at least 6 IntegerExpressions, got " + ints, ints >= 6);
        assertTrue("Expected at least 4 CharacterExpressions, got " + chars, chars >= 4);
    }

    @Test
    public void testBooleanConstantFoldingRemovesLiteralComparisons() {
        SourceFile source = SourceFile.fromResource("/booleans-to-fold.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        Program p = parser.parseProgram();
        assertEquals("Syntactic errors present in booleans-to-fold.tri", 0, reporter.getNumErrors());

        // Run checker before folding to populate decl/type as needed
        Checker checker = new Checker(reporter);
        checker.check(p);
        assertEquals("Contextual errors before folding", 0, reporter.getNumErrors());

        // Apply constant folding
        p.visit(new ConstantFolder());

        // Re-run checker to ensure types/decls are consistent post-transform
        checker.check(p);
        assertEquals("Contextual errors after folding", 0, reporter.getNumErrors());

        // After folding, there should be no BinaryExpression nodes where both children are IntegerExpression
        // This indicates literal comparisons have been reduced to boolean constants (VnameExpression true/false).
        int remainingLiteralComparisons = countLiteralComparisons(p.C);
        assertEquals("Literal integer comparisons should be folded away", 0, remainingLiteralComparisons);
    }

    @Test
    public void testHoisterProducesLetAboveWhileAndReplacesInvariant() {
        SourceFile source = SourceFile.fromResource("/while-to-hoist.tri");
        Scanner scanner = new Scanner(source);
        ErrorReporter reporter = new ErrorReporter(true);
        Parser parser = new Parser(scanner, reporter);

        Program p = parser.parseProgram();
        assertEquals("Syntactic errors present in while-to-hoist.tri", 0, reporter.getNumErrors());

        // Perform hoisting directly on the top-level command
        Command transformed = Hoister.transform(p.C);
        p.C = transformed;

        // Expect there exists a LetCommand wrapping a WhileCommand somewhere in the tree
        LetCommand lc = findLetWrappingWhile(p.C);
        assertNotNull("Expected a LetCommand that wraps a WhileCommand after hoisting", lc);
        WhileCommand wc = (WhileCommand) lc.C;

        // Inside the while body, the assignment to 'b' should now use a VnameExpression (the hoisted temp)
        AssignCommand assignB = findAssignTo(wc.C, "b");
        assertNotNull("Expected to find assignment to 'b' inside while body", assignB);
        assertTrue("Assignment to 'b' RHS should reference hoisted temp", assignB.E instanceof VnameExpression);
    }

    // ---------- Helpers ----------

    private static int countLiteralComparisons(Command cmd) {
        if (cmd == null || cmd instanceof EmptyCommand) return 0;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            return countLiteralComparisons(sc.C1) + countLiteralComparisons(sc.C2);

        } else if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            return countLiteralComparisons(lc.C);

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            return countInExpression(wc.E) + countLiteralComparisons(wc.C);

        } else if (cmd instanceof IfCommand) {
            IfCommand ic = (IfCommand) cmd;
            return countInExpression(ic.E) + countLiteralComparisons(ic.C1) + countLiteralComparisons(ic.C2);

        } else if (cmd instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) cmd;
            return countInExpression(ac.E);

        } else {
            // Other command kinds not explicitly handled
            return 0;
        }
    }

    private static int countInExpression(Expression e) {
        if (e == null) return 0;

        if (e instanceof IntegerExpression) {
            // literal itself isn't a comparison; skip
            return 0;

        } else if (e instanceof VnameExpression) {
            return 0;

        } else if (e instanceof UnaryExpression) {
            UnaryExpression ue = (UnaryExpression) e;
            return countInExpression(ue.E);

        } else if (e instanceof IfExpression) {
            IfExpression ie = (IfExpression) e;
            return countInExpression(ie.E1) + countInExpression(ie.E2) + countInExpression(ie.E3);

        } else if (e instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) e;
            int leftIsInt = (be.E1 instanceof IntegerExpression) ? 1 : 0;
            int rightIsInt = (be.E2 instanceof IntegerExpression) ? 1 : 0;
            int self = (leftIsInt + rightIsInt == 2) ? 1 : 0;
            return self + countInExpression(be.E1) + countInExpression(be.E2);
        }

        // Other expression kinds not explicitly handled
        return 0;
    }

    private static AssignCommand findAssignTo(Command cmd, String baseIdentifier) {
        if (cmd == null || cmd instanceof EmptyCommand) return null;

        if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            AssignCommand left = findAssignTo(sc.C1, baseIdentifier);
            if (left != null) return left;
            return findAssignTo(sc.C2, baseIdentifier);

        } else if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            return findAssignTo(lc.C, baseIdentifier);

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            return findAssignTo(wc.C, baseIdentifier);

        } else if (cmd instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) cmd;
            String base = extractBaseIdentifier(ac.V);
            if (baseIdentifier.equals(base)) return ac;
            return null;

        } else {
            return null;
        }
    }

    private static LetCommand findLetWrappingWhile(Command cmd) {
        if (cmd == null || cmd instanceof EmptyCommand) return null;

        if (cmd instanceof LetCommand) {
            LetCommand lc = (LetCommand) cmd;
            if (lc.C instanceof WhileCommand) {
                return lc;
            }
            // Explore inside the let body as well
            LetCommand nested = findLetWrappingWhile(lc.C);
            if (nested != null) return nested;
            return null;

        } else if (cmd instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) cmd;
            LetCommand left = findLetWrappingWhile(sc.C1);
            if (left != null) return left;
            return findLetWrappingWhile(sc.C2);

        } else if (cmd instanceof WhileCommand) {
            WhileCommand wc = (WhileCommand) cmd;
            return findLetWrappingWhile(wc.C);
        }

        return null;
    }

    private static String extractBaseIdentifier(Vname v) {
        if (v instanceof SimpleVname) {
            return ((SimpleVname) v).I.spelling;
        }
        // Dot/Subscript not handled for this test
        return null;
    }
}