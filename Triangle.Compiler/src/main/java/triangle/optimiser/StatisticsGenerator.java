package triangle.optimiser;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.visitors.*;

public class StatisticsGenerator implements ExpressionVisitor<Void, Void, Void> {

    private int characterExpressionCount = 0;
    private int integerExpressionCount = 0;

    public void generateStatistics(Program ast) {
        ast.visit(this);
        System.out.println("Character Expressions Count: " + characterExpressionCount);
        System.out.println("Integer Expressions Count: " + integerExpressionCount);
    }

    @Override
    public Void visitCharacterExpression(CharacterExpression ast, Void arg) {
        characterExpressionCount++;
        return null;
    }

    @Override
    public Void visitIntegerExpression(IntegerExpression ast, Void arg) {
        integerExpressionCount++;
        return null;
    }

    @Override
    public Void visitVnameExpression(VnameExpression ast, Void arg) {
        
        return null;
    }

}