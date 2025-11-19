package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class DoubleClickCommand extends Command {

    public DoubleClickCommand(Vname vAST, SourcePosition position) {
        super(position);
        V = vAST;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg a) {
        return v.visitDoubleClickCommand(this, a);
    }

    public final Vname V;
}
