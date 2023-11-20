package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.*;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;

public class StatisticsVisitor {
    private int charCount = 0;
    private int intCount = 0;

    public void printCounts() {
        System.out.println("Character Expressions: " + charCount);
        System.out.println("Integer Expressions: " + intCount);
    }

    // Implement visit methods for each relevant AST node
    public Object visitProgram(Program ast, Object o) {
        // Your implementation here
        return null;
    }

    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        charCount++;
        return null;
    }

    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        intCount++;
        return null;
    }

    // Add more visit methods as needed
}
