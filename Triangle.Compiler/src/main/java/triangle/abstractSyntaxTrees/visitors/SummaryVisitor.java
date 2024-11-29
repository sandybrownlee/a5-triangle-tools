package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.expressions.*;

public class SummaryVisitor implements CommandVisitor, ExpressionVisitor, ProgramVisitor{

    private int BinaryExpressionsCount = 0;
    private int IfCommandsCount = 0;
    private int WhileCommandsCount = 0;

    public int getBinaryExpressions() {
        return BinaryExpressionsCount;
    }

    public int getWhileCommands() {
        return WhileCommandsCount;
    }

    public int getIfCommands() {
        return IfCommandsCount;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object arg) {
        BinaryExpressionsCount++;
        if (ast.E1 != null) {
            ast.E1.visit(this, arg);
        }
        if (ast.E2 != null) {
            ast.E2.visit(this, arg);
        }
        return null;
    }
    public Object visitIfCommands(IfCommand ast, Object arg){
        IfCommandsCount++;
        if (ast.C1 != null){
            ast.C1.visit(this, arg);
        }
        if (ast.C2 != null){
            ast.C2.visit(this, arg);
        }
        if (ast.E != null){
            ast.E.visit(this, arg);
        }
        return null;
    }

    public Object visitWhileCommands(WhileCommand ast, Object arg){
        WhileCommandsCount++;
        if (ast.C != null){
            ast.C.visit(this, arg);
        }
        if (ast.E != null){
            ast.E.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitAssignCommand(AssignCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitIfCommand(IfCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitLetCommand(LetCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitWhileCommand(WhileCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitLoopWhileCommand(LoopWhileCommand loopWhileCommand, Object o) {
        return null;
    }

    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
        return null;
    }

    @Override
    public Object visitProgram(Program ast, Object arg) {
        ast.C.visit(this, arg);
        return null;
    }
}
