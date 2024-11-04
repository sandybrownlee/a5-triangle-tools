package triangle.ast;

import java.util.List;

public record Program(List<Statement> statements) {

    interface Visitor<T, E extends Exception> {

        T visit(Program program) throws E;

    }

}
