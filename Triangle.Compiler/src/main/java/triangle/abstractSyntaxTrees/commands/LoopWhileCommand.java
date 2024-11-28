package triangle.abstractSyntaxTrees.commands;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command{

    public final Command C1; //first command block
    public Expression E; // the loop condition
    public final Command C2; // the command block executed if the condition is true

    public LoopWhileCommand(Command c1AST, Expression eAST, Command c2AST, SourcePosition position) {
        super(position); //call superclass
        C1 = c1AST; //assign the first command
        E = eAST; // assign th eloop condition
        C2 = c2AST; // assign the second command
    }


    //accept visitor to process loopWhile command
    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }


}
