package triangle.ast;


import triangle.types.RuntimeType;

public interface Typeable {
    void setType(RuntimeType type);
    RuntimeType getType();
}
