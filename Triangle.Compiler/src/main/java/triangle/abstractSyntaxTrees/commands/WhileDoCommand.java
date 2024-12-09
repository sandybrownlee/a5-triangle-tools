package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class WhileDoCommand extends Command {

    public Command Cmd1;
    public Command Cmd2;
    public Expression Exp;

    public WhileDoCommand(Command Cmd1, Expression Exp, Command Cmd2, SourcePosition pos) {
        super(pos);
        this.Cmd1 = Cmd1;
        this.Cmd2 = Cmd2;
        this.Exp = Exp;
    }

    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
		return v.visitWhileDoCommand(this, arg);
	}
}

