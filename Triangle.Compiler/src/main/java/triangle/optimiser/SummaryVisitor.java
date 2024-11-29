package triangle.optimiser;

import triangle.optimiser.ConstantFolder;

public class SummaryVisitor extends ConstantFolder {

	public int binaryExpression_count;
	public int ifCommand_count;
	public int whileCommand_count;

	public SummaryVisitor() {
		binaryExpression_count = 0;
		ifCommand_count = 0;
		whileCommand_count = 0;
	}

	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
		super.visitBinaryExpression(ast, arg);
		binaryExpression_count++;
	}


	@Override
	public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
		super.visitIfCommand(ast, arg);
		ifCommand_count++;
	}

	
	@Override
	public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
		super.visitWhileCommand(ast, arg);
		whileCommand_count ++;
	}

}