package triangle.optimiser;

import java.util.HashSet;
import java.util.Set;

import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.optimiser.MutationCollector;

/**
 * Walks a loop body to collect every variable name that is assigned to.
 * These “mutated” names cannot be used as loop‐invariant expressions.
 */
public class MutationCollector implements CommandVisitor<Void, Void> {

  /** Set of all variable names mutated inside the loop */
  public final Set<String> mutated = new HashSet<>();

  @Override
  public Void visitAssignCommand(AssignCommand ast, Void arg) {
    // If the left‐hand side is a simple variable, record its name
    if (ast.V instanceof SimpleVname) {
      String name = ((SimpleVname) ast.V).I.spelling;
      mutated.add(name);
    }
    // Recurse into the RHS expression in case it contains nested commands
    ast.E.visit(this, null);
    return null;
  }

  @Override
  public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
    // Process first, then second command
    ast.C1.visit(this, null);
    ast.C2.visit(this, null);
    return null;
  }

  @Override
  public Void visitWhileCommand(WhileCommand ast, Void arg) {
    // We scan only this loop: first the condition, then the body
    ast.E.visit(this, null);
    ast.C.visit(this, null);
    return null;
  }

  // --- stubs for other commands (just recurse or do nothing) ---

  @Override public Void visitEmptyCommand(triangle.abstractSyntaxTrees.commands.EmptyCommand ast, Void arg) { return null; }
  @Override public Void visitCallCommand(triangle.abstractSyntaxTrees.commands.CallCommand ast, Void arg) { return null; }
  @Override public Void visitIfCommand(triangle.abstractSyntaxTrees.commands.IfCommand ast, Void arg) { return null; }
  @Override public Void visitLetCommand(triangle.abstractSyntaxTrees.commands.LetCommand ast, Void arg) { return null; }
 
}
