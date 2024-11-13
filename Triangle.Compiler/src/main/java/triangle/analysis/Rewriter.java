package triangle.analysis;

import triangle.repr.Statement;

public class Rewriter {
    public Statement rewrite(Statement stmt) {
        switch (stmt) {
            case Statement.AssignStatement assignStatement -> { }
            case Statement.ExpressionStatement expressionStatement -> { }
            case Statement.IfStatement ifStatement -> { }
            case Statement.LetStatement letStatement -> { }
            case Statement.LoopWhileStatement loopWhileStatement -> { }
            case Statement.RepeatUntilStatement repeatUntilStatement -> { }
            case Statement.RepeatWhileStatement repeatWhileStatement -> { }
            case Statement.StatementBlock statementBlock -> { }
            case Statement.WhileStatement whileStatement -> { }
        }
    }
}
