package triangle.ast;

public interface ProgramVisitor<T> {
    T visit(Program program);
}
