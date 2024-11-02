package triangle.ast;

import java.util.List;

public record Program(List<Statement> statements) {
    interface Visitor<T> {
        T visit(Program program);
    }
}
