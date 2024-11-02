package triangle.ast;

import java.util.List;

sealed public interface AST permits AST.Program, Statement {

    record Program(List<Statement> statements) implements AST { }

    interface Visitor {
        void visit(AST ast);
    }
}
