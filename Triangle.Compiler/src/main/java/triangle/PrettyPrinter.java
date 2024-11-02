package triangle;

import triangle.ast.Program;
import triangle.ast.ProgramVisitor;

public final class PrettyPrinter implements ProgramVisitor {

    @Override public void visit(final Program program) {
        visit(program.statements());
    }

}
