package triangle.optimiser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.Declaration;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;

/**
 * 1) Finds variables mutated inside each while‐loop (via MutationCollector).
 * 2) Scans for assignments whose RHS has *no* mutated var reference (via ExprRefs).
 * 3) Lifts those RHS expressions into fresh consts just before the loop.
 */
public class HoistingTransformer
    implements ProgramVisitor<Void, AbstractSyntaxTree>,
               CommandVisitor<Void, Command> {

  private int tmpCounter = 0;

  @Override
  public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
    // Transform the top‐level command
    ast.C = ast.C.visit(this, null);
    return ast;
  }

  @Override
  public Command visitWhileCommand(WhileCommand ast, Void arg) {
    // 1) collect mutated vars
    MutationCollector mc = new MutationCollector();
    ast.C.visit(mc, null);
    Set<String> mutated = mc.mutated;

    // 2) find and hoist any invariant assignments
    List<ConstDeclaration> hoisted = new ArrayList<>();
    Command newBody = hoistInvariants(ast.C, mutated, hoisted);

    // 3) if we hoisted anything, wrap in a let … in … block
    if (!hoisted.isEmpty()) {
      // chain multiple consts into one Declaration
      Declaration allConsts = hoisted.stream()
        .reduce((d1,d2) -> new triangle.abstractSyntaxTrees.declarations.SequentialDeclaration(d1,d2))
        .get();
      return new LetCommand(allConsts,
                            new WhileCommand(ast.E, newBody, ast.pos),
                            ast.pos);
    }

    // no change
    ast.C = newBody;
    return ast;
  }

  /** Recursively scan commands to hoist assignments with invariant RHS. */
  private Command hoistInvariants(Command cmd,
                                  Set<String> mutated,
                                  List<ConstDeclaration> hoisted) {
    if (cmd instanceof SequentialCommand) {
      SequentialCommand seq = (SequentialCommand)cmd;
      Command c1 = hoistInvariants(seq.C1, mutated, hoisted);
      Command c2 = hoistInvariants(seq.C2, mutated, hoisted);
      return new SequentialCommand(c1, c2, seq.pos);
    }

    if (cmd instanceof AssignCommand) {
      AssignCommand asg = (AssignCommand)cmd;
      // 2a) check if RHS mentions any mutated var
      boolean hasRef = asg.E.visit(new ExprRefs(mutated), null);
      if (!hasRef) {
        // 2b) create a new temp const
        String tmpName = "hoist" + (tmpCounter++);
        ConstDeclaration cd = new ConstDeclaration(
          new Identifier(tmpName, asg.pos),
          asg.E,
          asg.pos
        );
        hoisted.add(cd);

        // 2c) replace RHS with a reference to tmpName
        SimpleVname v = new SimpleVname(new Identifier(tmpName, asg.pos), asg.pos);
        return new AssignCommand(v, new VnameExpression(v, asg.pos), asg.pos);
      }
    }

    // other commands left unchanged
    return cmd;
  }

 
  @Override public Command visitSequentialCommand(SequentialCommand ast, Void arg) { return ast; }
  @Override public Command visitAssignCommand(AssignCommand ast, Void arg)     { return ast; }
  @Override public Command visitIfCommand(triangle.abstractSyntaxTrees.commands.IfCommand ast, Void arg) { return ast; }
  @Override public Command visitLetCommand(triangle.abstractSyntaxTrees.commands.LetCommand ast, Void arg) { return ast; }

@Override
public Command visitCallCommand(CallCommand ast, Void arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Command visitEmptyCommand(EmptyCommand ast, Void arg) {
	// TODO Auto-generated method stub
	return null;
}
  
}
