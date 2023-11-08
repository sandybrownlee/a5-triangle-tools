package triangle.optimiser;

import triangle.StdEnvironment;
import java.util.ArrayList;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.LoopCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.visitors.ActualParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.ActualParameterVisitor;
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.FormalParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.IdentifierVisitor;
import triangle.abstractSyntaxTrees.visitors.LiteralVisitor;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.abstractSyntaxTrees.visitors.VnameVisitor;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;
import triangle.contextualAnalyzer.IdentificationTable;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;

public class WhileHoister implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
		ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
		CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
		ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
		IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
		OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
		RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
		VnameVisitor<Void, AbstractSyntaxTree> {
	{

	}
	

	HoistIdentificationTable hoistTable = new HoistIdentificationTable();
	private Boolean hoisting = false;
	private Boolean inLoop = false;
	
	
	public class  TempVariable {
		public AbstractSyntaxTree V;
		public AbstractSyntaxTree E;
		public TempVariable(VnameExpression v, AbstractSyntaxTree e) {
			this.V = v;
			this.E = e;
		}
	}
	
	ArrayList<TempVariable> newConstants = new ArrayList<TempVariable>();
	

	

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
		
		
		
		
		return ast;
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
		hoistTable.openScope();
		System.out.println("FIRST PASS ");
		// First pass creates identifier table
		ast.C.visit(this);
		
		// Second pass optimises invariates 
		System.out.println("\nSECOND PASS ");
		hoisting = true;
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
		
		if (hoisting && inLoop) {
			// Implement checks
			if (ast.E1 instanceof BinaryExpression && ast.E1 == null) {
				return null;
			}
			if (ast.E2 instanceof BinaryExpression && ast.E2 == null) {
				return null;
			}
			
			
			if (hoistBinaryExpression(ast.E1, ast.E2)) {
		
				return ast;
			}
				
			
		}
		
		/*
		if (replacement1 != null && replacement2 != null) {
			return hoistBinaryExpression(replacement1, replacement2, ast.O);
		} else if (replacement1 != null) {
			ast.E1 = (Expression) replacement1;
		} else if (replacement2 != null) {
			ast.E2 = (Expression) replacement2;
		}

		*/
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
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		AbstractSyntaxTree replacement3 = ast.E3.visit(this); 
		
		return null;
	}

	@Override
	public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
		return ast;
	}

	@Override
	public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {
		ast.D.visit(this);
		ast.E.visit(this);
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
		
		if (!hoisting && inLoop) {
			
			SimpleVname vAst = (SimpleVname) ast.V.visit(this);
			
			hoistTable.enter(vAst.I.spelling, null);

		
			} 
		
		
		
		return ast;
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
		SimpleVname vAst = (SimpleVname) ast.V.visit(this);
		
		
		if (!hoisting && inLoop) {
			if (ast.V.variable) {
				
				hoistTable.enter(vAst.I.spelling, null);
				hoistTable.setMutated(((SimpleVname) ast.V).I.spelling, false);	
				}
			} 
		
		if (hoisting && inLoop) {
			if (replacement != null) {
				// Create a variable and assign it the value of the hoistable expression
				// Push the new variable to a stack and replace the old expression with it
				Identifier hoistedValueIdentifier = new Identifier(( "tmp"+newConstants.size()), ast.E.getPosition());
				hoistedValueIdentifier.type = StdEnvironment.integerType;
				SimpleVname hoistedValueWrapper = new SimpleVname(hoistedValueIdentifier, ast.E.getPosition());
				hoistedValueWrapper.type = StdEnvironment.integerType;
				VnameExpression hoistedVname = new VnameExpression(hoistedValueWrapper, ast.E.getPosition()); 
				hoistedVname.type = StdEnvironment.integerType;
				
				// Commented out as I've not been able to declare the temporary variable before the loop
				//ast.E = foldedValueNode;
				
				
				TempVariable temp = new TempVariable(hoistedVname, replacement);
				newConstants.add(temp);
				return replacement;
			}
		}
		
		 
		
		
	
		
		
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
		
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
		
		AbstractSyntaxTree replacement1 = ast.C1.visit(this);
		AbstractSyntaxTree replacement2 = ast.C2.visit(this);
		
		
		if (!hoisting) {
			return null;
		}
		
		if (ast.C1 instanceof WhileCommand) {
			System.out.println(replacement1);
			
			
			
		}
		if (ast.C2 instanceof WhileCommand) {
			SimpleVname newVname = (SimpleVname) ((VnameExpression) newConstants.get(0).V).V;
			BinaryExpression newVnameValue = (BinaryExpression) newConstants.get(0).E;
			AssignCommand assignNewVname = new AssignCommand(newVname, newVnameValue, ast.getPosition());
			
			
			SequentialCommand gen = new SequentialCommand(assignNewVname, ast.C2, ast.getPosition());
			
			
			
			VarDeclaration vari = new VarDeclaration(newVname.I, StdEnvironment.integerType, ast.getPosition());
			LetCommand let = new LetCommand(vari, gen, ast.getPosition() );
			
			SequentialCommand nodeReplacement = new SequentialCommand(ast.C1, let, ast.getPosition());
			
			// Can't change because C2 is final
			//ast.C2 = nodeReplacement;
			
			return nodeReplacement;
		
		}
		//ast.C1.visit(this);
		//ast.C2.visit(this);
		return null;
	}

	@Override
	public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
		
		ast.E.visit(this);

		Boolean firstWhileLoop = false;
		
		if (!inLoop) {
			firstWhileLoop = true;
			inLoop = true;
		}
		
		AbstractSyntaxTree replacement = ast.C.visit(this);


		if (firstWhileLoop) {
			
			inLoop = false;
		}
		

		return ast;
	}


	@Override
	public AbstractSyntaxTree visitLoopCommand(LoopCommand ast, Void arg) {
		//ast.C.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(this);
		if (replacement != null) {
			ast.E = (Expression) replacement;
		}
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

	public Boolean hoistBinaryExpression(AbstractSyntaxTree node1, AbstractSyntaxTree node2) {
		Boolean canHoist = true;
		
		if ((node1 instanceof VnameExpression)) {
			if (((VnameExpression) node1).V.variable) {
				canHoist = !hoistTable.checkMutated((( (SimpleVname) ((VnameExpression) node1).V).I.spelling), false);
				
				String variableSpelling = (( (SimpleVname) ((VnameExpression) node1).V).I.spelling);
				System.out.println("Testing | " + variableSpelling + " can hoist: "+ canHoist);
			}
		}
		
		if (!canHoist) { return false; }
		
		if ((node2 instanceof VnameExpression)) {
			if (((VnameExpression) node1).V.variable) {
				canHoist = !hoistTable.checkMutated((( (SimpleVname) ((VnameExpression) node1).V).I.spelling), false);
				
				String variableSpelling = (( (SimpleVname) ((VnameExpression) node1).V).I.spelling);
				System.out.println("Testing | " + variableSpelling + " can hoist: "+ canHoist);
			}
		}
		
		if (!canHoist) { return false; }
		
		// Create a new Vname with the value of the binary expression and return it
		
		
		
		
		return true;	
	}
	
	public AbstractSyntaxTree hoistWhileLoop(AbstractSyntaxTree e, AbstractSyntaxTree c) {
		if (hoisting) {
			
		} else {
		
		}
		
		
		return null;	
	}

}
