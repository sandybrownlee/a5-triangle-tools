package triangle.ast;

import java.util.List;

public record Program(List<Statement> statements) {
    interface Visitor<ST,T> {
        T visit(ST state, Program program);
    }
}
