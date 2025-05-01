package triangle.optimiser;

import java.util.Set;

import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;

/**
 * Checks whether an expression mentions any variable from a given set.
 * Used to detect loop‐invariant expressions.
 */
public class ExprRefs implements ExpressionVisitor<Boolean, Void> {

  private final Set<String> mutated;

  /** @param mutated names of variables we consider “changing” */
  public ExprRefs(Set<String> mutated) {
    this.mutated = mutated;
  }

  public Boolean visitVnameExpression(VnameExpression ast, Void arg) {
    // If this is a simple variable, check if it’s mutated
    if (ast.V instanceof SimpleVname) {
      String name = ((SimpleVname) ast.V).I.spelling;
      return mutated.contains(name);
    }
    return false;
  }

  @Override
  public Boolean visitBinaryExpression(BinaryExpression ast, Void arg) {
    // Invariant only if neither subexpression mentions a mutated var
    return ast.E1.visit(this, null) || ast.E2.visit(this, null);
  }

  @Override
  public Boolean visitIntegerExpression(IntegerExpression ast, Void arg) {
    return false;  // literal never references a var
  }

  @Override
  public Boolean visitCharacterExpression(CharacterExpression ast, Void arg) {
    return false;  // literal never references a var
  }

  @Override
  public Boolean visitIfExpression(IfExpression ast, Void arg) {
    // test, then-branch, else-branch
    return ast.E1.visit(this,null)
        || ast.E2.visit(this,null)
        || ast.E3.visit(this,null);
  }

@Override
public Void visitArrayExpression(ArrayExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitBinaryExpression(BinaryExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitCallExpression(CallExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitCharacterExpression(CharacterExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitEmptyExpression(EmptyExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitIfExpression(IfExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitIntegerExpression(IntegerExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitLetExpression(LetExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitRecordExpression(RecordExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitUnaryExpression(UnaryExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Void visitVnameExpression(VnameExpression ast, Boolean arg) {
	// TODO Auto-generated method stub
	return null;
}

  // --- stub out or recurse for all other expression types ---
  // e.g. visitLetExpression, visitUnaryExpression, etc.
}
