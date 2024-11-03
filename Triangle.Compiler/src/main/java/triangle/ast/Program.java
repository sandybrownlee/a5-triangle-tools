package triangle.ast;

import java.util.List;

public record Program(List<Statement> statements) {

    interface Visitor<ST, T, E extends Exception> {

        T visit(ST state, Program program) throws E;

    }

}
