package triangle.optimiser;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.contextualAnalyzer.IdentificationTable;
import triangle.syntacticAnalyzer.SourcePosition;

import java.util.Stack;

public class HoistVisitor extends ConstantFolder {

	private final IdentificationTable idTable = new IdentificationTable();
	private boolean currentlyHoisting = false;
	private boolean currentlyInLoop = false;
	private int numberOfConstants = 0;
	Stack<AbstractSyntaxTree> preDeclarationStack = new Stack<>();
	Stack<AbstractSyntaxTree> postDeclarationStack = new Stack<>();

	@Override
	public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg) {
		ast.I.visit(this);
		return ast;
	}

	/**
	 * Opens the scope of the identification table present within this visitor,
	 * Visits the AST which collects variables to update and submits a
	 *
	 * @param ast
	 * @param arg
	 * @return
	 */
	@Override
	public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
		idTable.openScope();
		ast.C.visit(this);
		currentlyHoisting = true;
		ast.C.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement1 = ast.E1.visit(this);
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		ast.O.visit(this);

		// this doesn't allow the hoisting of binary expressions where i.e. "b:=b + (c+y)", where c and y are invariants,
		if (currentlyHoisting && currentlyInLoop) {
			if (replacement1 instanceof VnameExpression && replacement2 instanceof VnameExpression) {
				if (canHoistExpression(replacement1) && canHoistExpression(replacement2)) {
					return ast;
				}
			}
			else if (replacement1 instanceof BinaryExpression binaryExpression2 && replacement2 instanceof IntegerExpression) {
				AbstractSyntaxTree node1 = binaryExpression2.E1;
				AbstractSyntaxTree node2 = binaryExpression2.E2;
				if (canHoistBinaryExpression(node1, node2)) {
					return ast;
				}
			}
			else if (replacement1 instanceof BinaryExpression binaryExpression1 && replacement2 instanceof BinaryExpression binaryExpression2) {
				AbstractSyntaxTree node1 = binaryExpression1.E1;
				AbstractSyntaxTree node2 = binaryExpression1.E2;

				AbstractSyntaxTree node3 = binaryExpression2.E1;
				AbstractSyntaxTree node4 = binaryExpression2.E2;

				if (canHoistBinaryExpression(node1, node2) && canHoistBinaryExpression(node3, node4)) {
					return ast;
				}
			}
			else if (replacement1 instanceof BinaryExpression binaryExpression1) {
				AbstractSyntaxTree node1 = binaryExpression1.E1;
				AbstractSyntaxTree node2 = binaryExpression1.E2;
				if (canHoistBinaryExpression(node1, node2)) {
					return replacement1;
				}
			}
			else if (replacement2 instanceof BinaryExpression binaryExpression1) {
				AbstractSyntaxTree node1 = binaryExpression1.E1;
				AbstractSyntaxTree node2 = binaryExpression1.E2;
				if (canHoistBinaryExpression(node1, node2)) {
					return replacement2;
				}
			}
			else if (replacement1 instanceof VnameExpression) {
				if (canHoistExpression(replacement1)) {
					return ast;
				}
			}
		}
		return null;
	}

	/**
	 * This function returns true if the entry does not exist within the identification table
	 * 	 * (i.e. a and b exists within the table as it exists within "assign command" since their values are updated.
	 *
	 * However C does not exist within the table as it is not visited by assign command.
	 * @param ast The expression which is to be checked.
	 * @return true if entry doesnt exist within identification table, false if exists within identification table.
	 */
	public boolean canHoistExpression(AbstractSyntaxTree ast) {
		if (ast instanceof VnameExpression vnameExpression) {
			AbstractSyntaxTree simpleVname = vnameExpression.V.visit(this);

			// we want to grab the variables spelling to check against the table and so express it as a simple vName,
			// there is probably opportunity to replace this with the equivalent of  Vname v = new SimpleVName()
			// simpleVname works because we literally just want access to the Identifier present within the wrapper class.
			SimpleVname realSimpleVname = (SimpleVname) simpleVname;
			String spelling = realSimpleVname.I.spelling;
			Declaration d = idTable.retrieve(spelling, false);
			return d == null;
		}
		else if (ast instanceof BinaryExpression binaryExpression) {
			if (canHoistBinaryExpression(binaryExpression.E1, binaryExpression.E2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method returns true if both expressions within a binary expression are vNameExpressions and
	 * @param node1
	 * @param node2
	 * @return
	 */
	public boolean canHoistBinaryExpression(AbstractSyntaxTree node1, AbstractSyntaxTree node2) {
		if (node1 instanceof VnameExpression && node2 instanceof VnameExpression) { // an example would be "b := b + (c + y); ! c+y can be hoisted"
			return canHoistExpression(node1) && canHoistExpression(node2);
		}
		// allows the hoisting of binary expressions within binary expressions - there's probably a better way to recursively do this an example would be "b := b + (c + 4); ! c+4 can be hoisted"
		else if (node1 instanceof VnameExpression && node2 instanceof IntegerExpression) { // to add a type of statement to be recursively checked, we can simply just check either node vs canHoistExpression()
			return canHoistExpression(node1);
		}
		else if (node1 instanceof BinaryExpression && node2 instanceof BinaryExpression) { // to add a type of statement to be recursively checked, we can simply just check either node vs canHoistExpression()
			return canHoistExpression(node1) && canHoistExpression(node2);
		}
		return false;
	}

	@Override
	public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
		ast.V.visit(this);
		return ast;
	}

	@Override
	public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		AbstractSyntaxTree vName = ast.V.visit(this);
		SimpleVname vAST = (SimpleVname) vName;

		if (replacement != null) {
			ast.E = (Expression) replacement;
		}

		// while we're not actively hoisting we want to add identifiers present while inside of a loop,
		if (!currentlyHoisting && currentlyInLoop) {
			// we only care about variables
			if (vAST.variable) {
				String spelling = vAST.I.spelling;
				Declaration d = new VarDeclaration(vAST.I, vAST.type, ast.getPosition());

				idTable.enter(spelling, d);
			}
		}
		else if (currentlyHoisting && currentlyInLoop) {
			// nullability check as binary expression evaluation can and will return null if there is no invariant present in the current assignment.
			if (replacement != null) {
				// logic for creating an expression to hoist and pushing to the preDeclarationStack.
				// there's honestly no need for this pos assignment here other than to reduce the number of calls
				// within the code as I genuinely don't know whether it matters or not other than for error messages, but it's impossible to predict the new position
				// where the assignments, etc will end up in the tree as this is recursively doing this DFS.
				SourcePosition pos = ast.getPosition();
				Expression e = (Expression) replacement;

				// we know that this new identifier is going to be of integer type, and we assign it a name of temp + number of constants so that
				// if there is more than 1 value to hoist in a single loop we avoid duplication (in triangle's case complete omission of these variables).
				Identifier id = new Identifier("temp"+numberOfConstants, pos);
				id.decl = e.type;
				id.type = e.type;
				numberOfConstants++;

				// we now need to create a variable wrapper (vname expression) to create an assignment command with.
				SimpleVname newVar = new SimpleVname(id, pos);
				newVar.type = StdEnvironment.integerType;

				// we can pass simplevname as the vname since its just a sub-class, and then the expression which would be assigned normally
				// i.e. "C+2"
				AssignCommand assignmentToInject = new AssignCommand(newVar, e, pos);
				preDeclarationStack.push(assignmentToInject);

				// now we need to replace the existing expression with the identifier as a vNameExpression
				VnameExpression vEAST = new VnameExpression(newVar, ast.E.getPosition());
				vEAST.type = StdEnvironment.integerType;
				ast.E = vEAST;
			}
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
		AbstractSyntaxTree node1 = ast.C1.visit(this);
		AbstractSyntaxTree node2 = ast.C2.visit(this);

		if (node1 != null) {
			// removing final on command class to implement this.
			ast.C1 = (Command) node1;
		}

		if (currentlyHoisting) {
			if (node2 instanceof WhileCommand) {
				SourcePosition pos = ast.getPosition();
				Declaration sequentialDeclaration = createSequentialDeclaration(pos);
				SequentialCommand sequentialSubTreeToInject;
				Command assignCommands = createSequentialCommand(pos);
				sequentialSubTreeToInject = new SequentialCommand(assignCommands, ast.C2, pos);
				LetCommand letCommand = new LetCommand(sequentialDeclaration, sequentialSubTreeToInject, pos);
				// we now have a let command and access to the while command, to place these together we wrap them in a sequential command
				// we now set the parent tree equal to the combined let trees in sequential command form



				// and then to add this back to the tree, we pass a new sequential command which contains the updated subtree
				// and the rest of the tree on the left as we're doing DFS algorithms to figure out the program layout.
				return new SequentialCommand(ast.C1, letCommand, pos);
			}
		}
		return null;
	}

	public Command createSequentialCommand(SourcePosition pos) {
		AssignCommand assignCommand = (AssignCommand) createAssignmentCommand(pos);
		if (assignCommand != null) {
			return new SequentialCommand(createSequentialCommand(pos), assignCommand, pos);
		}
		return new EmptyCommand(pos);
	}

	public Command createAssignmentCommand(SourcePosition pos) {
		if (postDeclarationStack.isEmpty()) {
			return null;
		}
        return (AssignCommand) postDeclarationStack.pop();
	}


	/**
	 * Below is possible solutions to the problem of Let commands & sequential commands rather than sequential declarations
	 * @param pos
	 * @return
	 */
	public Declaration createSequentialDeclaration(SourcePosition pos) {

		VarDeclaration varDeclaration = null;
		if (preDeclarationStack.size() == 1) {
			varDeclaration = createVarDeclaration();
			return varDeclaration;
		}
		if (preDeclarationStack.size() == 2) {
			varDeclaration = createVarDeclaration();
			VarDeclaration varDeclaration2 = createVarDeclaration();

			return new SequentialDeclaration(varDeclaration, varDeclaration2, pos);
		}
		else {
			varDeclaration = createVarDeclaration();
			return new SequentialDeclaration(createSequentialDeclaration(pos), varDeclaration, pos);
		}
	}

	public VarDeclaration createVarDeclaration() {
		if (preDeclarationStack.isEmpty()) {
			return null;
		}

		AssignCommand assignmentToInject = (AssignCommand) preDeclarationStack.pop();
		SourcePosition pos = assignmentToInject.getPosition();
		postDeclarationStack.push(assignmentToInject);

		// we create a variable, "temp" for example, and then wrap it inside a let command.
		SimpleVname sAST = (SimpleVname) assignmentToInject.V;
		Identifier id = sAST.I;
		return new VarDeclaration(id, id.type, pos);
	}

	@Override
	public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
		currentlyInLoop = true; // set true before visiting otherwise logic is completely skipped.
		AbstractSyntaxTree cReplacement = ast.C.visit(this);
		AbstractSyntaxTree eReplacement = ast.E.visit(this);
		if (eReplacement != null) {
			ast.E = (Expression) eReplacement;
		}
		currentlyInLoop = false;
		return ast;
	}

	/**
	 // assistance function for foldBooleanExpression.
	 public AbstractSyntaxTree unwrapIdentifier(AbstractSyntaxTree ast) {
	 VnameExpression vNameAST = (VnameExpression) ast;
	 SimpleVname simpleVnameAST = (SimpleVname) vNameAST.V;
	 return simpleVnameAST.I.decl;
	 }

	 // started work on how to fold booleans further such that => b := true; if b \/ ((1+2)=3) would resolve to "if true" - but then realised this is a little out of scope, but folding is cool :)
	 // didn't fully implement this as I caught myself doing more work than is required for the assignment.
	 public AbstractSyntaxTree foldBooleanExpression(AbstractSyntaxTree ast1, AbstractSyntaxTree ast2, Operator o) {
	 boolean b1 = Boolean.parseBoolean(unwrapIdentifier(ast1).toString());
	 boolean b2 = Boolean.parseBoolean(unwrapIdentifier(ast2).toString());

	 Identifier foldedValue;
	 Identifier foldedValue1 = new Identifier("true", ast1.getPosition());
	 Identifier foldedValue2 = new Identifier("false", ast2.getPosition());

	 foldedValue = b1 || b2 ? foldedValue1  : foldedValue2;


	 return foldedValue;
	 }
	 **/
}