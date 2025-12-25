/*
 * @(#)WhileDoCommand.java      
 */

package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class WhileDoCommand extends Command {
    public final Command command1;
    public final Expression expression;
    public final Command command2;

    public WhileDoCommand(Command command1, Expression expression, Command command2, SourcePosition position) {
        super(position);
        this.command1 = command1;
        this.expression = expression;
        this.command2 = command2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitWhileDoCommand(this, arg);
    }
}

