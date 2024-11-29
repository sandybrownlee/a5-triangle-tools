package triangle.optimiser;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.*;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.formals.*;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.*;
import triangle.abstractSyntaxTrees.visitors.*;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Hoisting implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
		ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
		CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
		ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
		IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
		OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
		RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
		VnameVisitor<Void, AbstractSyntaxTree> {


	//checks if it's inside the while loop
	boolean InsideWhileLoop = false;

	//stores all assignments and expression from the file. This includes the initial assignments for the values
	LinkedHashMap<AssignCommand, Boolean> Assignments = new LinkedHashMap<>();

	/**
	 * This method converts the ast into sequential commands, which
	 * is passed to the recursive parsing method
	 * @param C is the AbstractSyntaxTree from the visitWhileCommand method
	 * @return null
     */
	public AbstractSyntaxTree ConvertVariables(AbstractSyntaxTree C) {
		SequentialCommand sq = (SequentialCommand) C;
		recursiveTreeTraversal(sq);
		return null;
	}

	/**
	 * This method recursively traverses the sequential command called SCommand. It adds the assignment
	 * commands using the addToHoistHashMap() method, which adds it to the hashmap.
	 * @param SCommand is the sequential command, which gets traversed through
	 * @return null
	 */
	public AbstractSyntaxTree recursiveTreeTraversal(SequentialCommand SCommand) {
		// this recursively traverses, if it's a sequential command
		if (SCommand.C1 instanceof SequentialCommand sequentialCommand) {
			recursiveTreeTraversal(sequentialCommand);
		}
		// this checks if it's an assigncommand, adding it to the hoist hashmap
		if (SCommand.C2 instanceof AssignCommand a) {
			addToHoistableHashMap(a);
		}
		return null;
	}

	/**
	 * This method adds the assignment command to the hashmap. The method ensures
	 * a structure of having the first expression eb the variable, and second one be the expression
	 * @param ACommand is the assignment command passed
	 */
	public void addToHoistableHashMap(AssignCommand ACommand) {
		// this swaps the order, making the variable e1, and the expression e2
		if (ACommand.E instanceof BinaryExpression b && b.E2 instanceof VnameExpression) {
			Expression tmp = b.E1;
			b.E1 = b.E2;
			b.E2 = tmp;
		}
		//this adds the assigncommand to the hoist hashmap
		this.Assignments.put(ACommand, false);
	}

	/**
	 * This method adds all variables before while loop to a hoist hashmap.
	 * This is later used to replace the values of the variables.
	 * @param A the assign command passed
	 */
	public void addVariablesToMap(AssignCommand A) {
		//this adds the assigncommand to the hoist hashmap
		this.Assignments.put(A, false);
	}

	/**
	 * This method returns the SimpleVname of an assignment command passed
	 * @param ACommand the assignment command passed
	 * @return the SimpleVname of the assignment command
	 */
	public SimpleVname getSimpleVname(AssignCommand ACommand) {
		//this adds the SimpleVname to the hoist hashmap
		return (SimpleVname) ACommand.V;
	}

	/**
	 * This method returns a BinaryExpression or a IntegerExpression of an
	 * assignment command passed
	 * @param ACommand the assignment command passed
	 * @return the BinaryExpression or IntegerExpression of the assignment command
	 */
	public Expression getBinaryOrIntegerExpression(AssignCommand ACommand) {
		//if the assign command is a BinaryExpression then return a BinaryExpression
		if (ACommand.E instanceof BinaryExpression BExpression) {
			return BExpression;
		}
		//else return a IntegerExpression
		return (IntegerExpression) ACommand.E;
	}

	/**
	 * This method returns the BinaryExpression as String. This uses the
	 * BinaryExpression passed, and appends its contents to a StringBuffer,
	 * which later gets returned. This returns the entire expression as a
	 * whole, rather than needing to use E1, E2 and O to get the separate parts.
	 * @param BinaryExp the BinaryExpression, which should be returned as a string
	 * @return the full BinaryExpression as a string
	 */
	public String getBinaryExpressionString(BinaryExpression BinaryExp) {
		//makes a new StringBuffer called BExpressions
		StringBuffer BExpressions = new StringBuffer();

		//gets the SimpleVName of the first part of the expression, and stores it as SVName
		String SVName = ((SimpleVname) ((VnameExpression) BinaryExp.E1).V).I.spelling;

		//if both parts are Variables then just return null, as its unhoistable
		if (BinaryExp.E2 instanceof VnameExpression) {
			return null;
		}

		//gets the IntegerExpression of the second part of the expression, and stores it as IExpression
		String IExpression = ((IntegerExpression) BinaryExp.E2).IL.spelling;

		//this appends each part, and the operator as well

		BExpressions.append(SVName); // the variable name appended
		BExpressions.append(BinaryExp.O.spelling); // the operator appended
		BExpressions.append(IExpression); // the integer expression appended

		//this returns it back as a string
		return BExpressions.toString();
	}

	/**
	 * This method verifies and checks if an entry into the Hashmap is hoitable. It checks this
	 * by adding a few error type checks, and a loop. The loop is the main check, ensuring that
	 * the expression can be hoisted and ensuring that it isn't dynamic, as that would make it
	 * unhoistable.
	 */
	public void isHoistAble() {
		// this loops through every entry in the hashmap
		for (HashMap.Entry<AssignCommand, Boolean> e : Assignments.entrySet()) {

			//this gets the current variables from the hashmap
			AssignCommand ACommand = e.getKey();

			//this checks if it is a IntegerExpression, skipping it as it doesn't need to be hoisted
			if (getBinaryOrIntegerExpression(ACommand) instanceof IntegerExpression) {
				continue;
			}

			//this gets lhs variable, which is an assignment variable
			String AssignmentVariable = this.getSimpleVname(ACommand).I.spelling;

			// this converts the binary expression to a string, checking if its null, as that would mean it doesn't need to be hoisted
			String ExpressionToString = this.getBinaryExpressionString((BinaryExpression) getBinaryOrIntegerExpression(e.getKey()));
			if (ExpressionToString == null) {
				continue;
			}

			// this checks if binary expression doesn't contain the variable of the lhs variable
			if (!ExpressionToString.contains(AssignmentVariable)) {

				//these lines go through a for loop, checking if the variable in the
				// expression has more than one once occurrence as an LHS variable, as
				// this would indicate that it shouldn't be hoisted due to its dynamic behaviour
				boolean flag = false;
				for (AssignCommand AssignmentVariableCommand : Assignments.keySet()) {
					if (ExpressionToString.contains(getSimpleVname(AssignmentVariableCommand).I.spelling)) {
						if (flag) {
							e.setValue(false);
							break;
						}
						flag = true;
					}
					e.setValue(true);
				}

			}
		}
	}

	/**
	 * This method gets the value of the assignment variables, by using a for each loop, to get its location
	 * @param variableName is the variable name that is being searched for in the hashmap
	 * @return the key of the variable, providing access to the variables value
	 */
	public AssignCommand getAssignmentValue(String variableName) {
		//this searches through entire hashmap
		for (HashMap.Entry<AssignCommand, Boolean> AssignmentValue : Assignments.entrySet()) {
			//this checks each key to find the variable we are searching for
			if (getSimpleVname(AssignmentValue.getKey()).I.spelling.equals(variableName))
				//when found, it returns the key hashmap key of it
				return AssignmentValue.getKey();
		}
		return null;
	}

	/**
	 * This method sets the value of the assignment. Following the same strucute as the get method,
	 * it searches through the hashmap, trying to find the variable. When found, it will then assign
	 * the hoisted expression to it.
	 * @param integerExpression is the expression we would like to add to the variable
	 * @param variableName is the variable name we are searching for
	 */
	public void setAssignmentValue(IntegerExpression integerExpression, String variableName) {
		//this searches through entire hashmap
		for (HashMap.Entry<AssignCommand, Boolean> AssignmentValue : Assignments.entrySet()) {
			//this checks each key to find the variable we are searching for
			if (getSimpleVname(AssignmentValue.getKey()).I.spelling.equals(variableName)) {
				//when found, it sets the expression passed as the new expression, and then stops the loop
				AssignmentValue.getKey().E = integerExpression;
				break;
			}
		}
	}

	/**
	 * This method completes the actual hoisting, by converting the hoisted variables
	 * into integer expressions. The method includes some more error checks, as well
	 * as the hoisted translations. It uses the hoisted hashmap to get all the
	 * hoistable expressions, as they are labeled true by their boolean values.
	 * @param C is the AbstractSyntaxtree passed
	 * @return null
	 */
	public AbstractSyntaxTree doHosting(AbstractSyntaxTree C){

		//checks if the expressions are hoistable
		isHoistAble();

		//this converts the variables to their values and replaces their expression in the tree
		for (HashMap.Entry<AssignCommand, Boolean> e : Assignments.entrySet()) {
			AssignCommand ACommand = e.getKey();

			//this checks if the expression is already a direct assignment, as it doesn't need to hoisted
			if (getBinaryOrIntegerExpression(ACommand) instanceof IntegerExpression || e.getValue() == false)
				continue;

			//this gets and stores the expression as a BinaryExpression
			BinaryExpression BExpression = (BinaryExpression) getBinaryOrIntegerExpression(ACommand);

			//this gets and stores the LHS variable using the getSimpleVname method, passing the assign command
			SimpleVname LHSVariableName = getSimpleVname(ACommand);

			//this checks if both of the expressions are IntegerExpressions already, requiring it to just be folded
			if (BExpression.E1 instanceof IntegerExpression && BExpression.E2 instanceof IntegerExpression) {

				//this folds the IntegerExpressions and sets the assignment value using the method call
				IntegerExpression IExpression = (IntegerExpression) BExpression.visit(this);
				ACommand.E = IExpression;
				setAssignmentValue(IExpression, LHSVariableName.I.spelling);
				continue;
			}

			//this checks if both of the expressions are VnameExpressions,requiring them to be translated,
			// wrapped and then set as the assignment value
			else if (BExpression.E1 instanceof VnameExpression && BExpression.E2 instanceof VnameExpression) {
				//this gets the spelling of both variables
				String RHSFirstVariable = ((SimpleVname) ((VnameExpression) BExpression.E1).V).I.spelling;
				String RHSSecondVariable = ((SimpleVname) ((VnameExpression) BExpression.E2).V).I.spelling;

				//this gets both of values of the variables, using the get method
				AssignCommand RHSFirstAssignment = getAssignmentValue(RHSFirstVariable);
				AssignCommand RHSSecondAssignment = getAssignmentValue(RHSSecondVariable);

				//this wraps them as IntegerExpressions
				IntegerExpression RHSFirstExpression = (IntegerExpression) RHSFirstAssignment.E;
				IntegerExpression RHSSecondExpression = (IntegerExpression) RHSSecondAssignment.E;

				//this replaces each expression with its associated variable value
				BExpression.E1 = RHSFirstExpression;
				BExpression.E2 = RHSSecondExpression;

				//this folds the IntegerExpressions together and uses the set value method
				// to set it as the lhs variables value
				IntegerExpression IExpression = (IntegerExpression) BExpression.visit(this);
				ACommand.E = IExpression;
				setAssignmentValue(IExpression, LHSVariableName.I.spelling);
				continue;
			}

			//if the expressions are variable name and a number, we only need to get the
			// value of the variable. This follows the same steps as the other cases.
			// It first gets the variable spelling and then uses the get method to get its value.
			String RHSAllVariables = ((SimpleVname) ((VnameExpression) BExpression.E1).V).I.spelling;
			AssignCommand rhsAssignCommand = getAssignmentValue(RHSAllVariables);

			//we then wrap it as a IntegerExpression, and replace the variable name with its value
			IntegerExpression RHSAllVariablesValue = (IntegerExpression) rhsAssignCommand.E;
			BExpression.E1 = RHSAllVariablesValue;

			//this folds the IntegerExpressions together and uses the set value method
			// to set it as the lhs variables value
			IntegerExpression IntegerExpression = (IntegerExpression) BExpression.visit(this);
			ACommand.E = IntegerExpression;
			setAssignmentValue(IntegerExpression, LHSVariableName.I.spelling);
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
		ast.I.visit(this);
		ast.FPS.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitVarFormalParameter(VarFormalParameter ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
		ast.FT.visit(this);
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitDotVname(DotVname ast, Void arg) {
		ast.I.visit(this);
		ast.V.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg) {
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSubscriptVname(SubscriptVname ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.V.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
		ast.IL.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
		ast.FT.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.I.visit(this);
		ast.RA.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
		ast.C.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitOperator(Operator ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitCharacterLiteral(CharacterLiteral ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIntegerLiteral(IntegerLiteral ast, Void arg) {
		return ast;
	}

	@Override
	public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
		ast.FP.visit(this);
		ast.FPS.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
		ast.FP.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void arg) {
		ast.AA.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement1 = ast.E1.visit(this);
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		ast.O.visit(this);

		// if visiting a child node returns something, it's either the original constant
		// (IntegerLiteral) or a folded version replacing the expression at that child
		// node
		// If both child nodes are not null; return a folded version of this
		// BinaryExpression
		// Otherwise, at least one child node isn't constant (foldable) so just replace
		// the
		// foldable child nodes with their folded equivalent and return null
		if (replacement1 != null && replacement2 != null) {
			return foldBinaryExpression(replacement1, replacement2, ast.O);
		} else if (replacement1 != null) {
			ast.E1 = (Expression) replacement1;
		} else if (replacement2 != null) {
			ast.E2 = (Expression) replacement2;
		}

		// if we get here, we can't fold any higher than this level
		return null;
	}

	@Override
	public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void arg) {
		ast.APS.visit(this);
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
		ast.CL.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void arg) {
		AbstractSyntaxTree replacement1 = ast.E1.visit(this);
		if (replacement1 != null) {
			ast.E1 = (Expression) replacement1;
		}
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		if (replacement2 != null) {
			ast.E2 = (Expression) replacement2;
		}
		AbstractSyntaxTree replacement3 = ast.E3.visit(this);
		if (replacement3 != null) {
			ast.E3 = (Expression) replacement3;
		}

		return null;
	}

	@Override
	public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
		return ast;
	}

	@Override
	public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {
		ast.D.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void arg) {
		ast.RA.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}

		ast.O.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
		ast.V.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
		ast.ARG1.visit(this);
		ast.ARG2.visit(this);
		ast.O.visit(this);
		ast.RES.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitFuncDeclaration(FuncDeclaration ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.FPS.visit(this);
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitProcDeclaration(ProcDeclaration ast, Void arg) {
		ast.C.visit(this);
		ast.FPS.visit(this);
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
		ast.D1.visit(this);
		ast.D2.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitTypeDeclaration(TypeDeclaration ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
		ast.ARG.visit(this);
		ast.O.visit(this);
		ast.RES.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void arg) {
		ast.I.visit(this);
		ast.T.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		//this checks if assignment command is called inside the while loop
		if (!InsideWhileLoop) {
			//adds the assignments to the variable hashmap
			addVariablesToMap(ast);
		}
		ast.V.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitCallCommand(CallCommand ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitEmptyCommand(EmptyCommand ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
		ast.C1.visit(this);
		ast.C2.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {
		ast.C.visit(this);
		ast.D.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
		ast.C1.visit(this);
		ast.C2.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
		this.InsideWhileLoop= true; // this changes the condition to say it's inside the while loop
		ConvertVariables(ast.C); //adds variables
		doHosting(ast.C); // calls the hosting method
		ast.C.visit(this); //visits the body of the code
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitDoWhileDoCommand(DoWhileDoCommand ast, Void arg) {
		ast.C1.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		ast.C2.visit(this);
		return null;
	}

	// TODO uncomment if you've implemented the repeat command
//	@Override
//	public AbstractSyntaxTree visitRepeatCommand(RepeatCommand ast, Void arg) {
//		ast.C.visit(this);
//		AbstractSyntaxTree replacement = ast.E.visit(this);
//		if (replacement != null) {
//			ast.E = (Expression) replacement;
//		}
//		return null;
//	}

	@Override
	public AbstractSyntaxTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
		ast.AA.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
		return null;
	}

	@Override
	public AbstractSyntaxTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
		ast.AP.visit(this);
		ast.APS.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
		ast.AP.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitConstActualParameter(ConstActualParameter ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		return null;
	}

	@Override
	public AbstractSyntaxTree visitFuncActualParameter(FuncActualParameter ast, Void arg) {
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitProcActualParameter(ProcActualParameter ast, Void arg) {
		ast.I.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitVarActualParameter(VarActualParameter ast, Void arg) {
		ast.V.visit(this);
		return null;
	}

	public AbstractSyntaxTree foldBinaryExpression(AbstractSyntaxTree node1, AbstractSyntaxTree node2, Operator o) {
		// the only case we know how to deal with for now is two IntegerExpressions
		if ((node1 instanceof IntegerExpression) && (node2 instanceof IntegerExpression)) {
			int int1 = (Integer.parseInt(((IntegerExpression) node1).IL.spelling));
			int int2 = (Integer.parseInt(((IntegerExpression) node2).IL.spelling));
			Object foldedValue = null;
			
			if (o.decl == StdEnvironment.addDecl) {
				foldedValue = int1 + int2;
			}
			if (o.decl == StdEnvironment.trueDecl) { //checks if the declared is a true
				foldedValue = true; //sets the fold value as true
			}
			if (o.decl == StdEnvironment.falseDecl) { //checks if the declared is a false
				foldedValue = false; //sets the fold value as false
			}

			if (foldedValue instanceof Integer) {
				IntegerLiteral il = new IntegerLiteral(foldedValue.toString(), node1.getPosition());
				IntegerExpression ie = new IntegerExpression(il, node1.getPosition());
				ie.type = StdEnvironment.integerType;
				return ie;
			} else if (foldedValue instanceof Boolean) {
				String Boolean = (boolean) foldedValue ? "true" : "false"; //sets the Boolean to either true or false depending on the foldedValue
				Identifier BooleanIdent = new Identifier(Boolean, node1.getPosition()); //makes a new Identifier called BooleanIdent containing spelling and position of it
				BooleanIdent.decl = (boolean) foldedValue ? StdEnvironment.trueDecl : StdEnvironment.falseDecl; //sets the declaration of it to either true of false, depending on the folded value
				SimpleVname svn = new SimpleVname(BooleanIdent, node1.getPosition()); //wrappers the identifier
				VnameExpression ve = new VnameExpression(svn, node1.getPosition()); //wraps it into an expression

				return ve; //returns the expression
			}
		}

		// any unhandled situation (i.e., not foldable) is ignored
		return null;
	}

}
