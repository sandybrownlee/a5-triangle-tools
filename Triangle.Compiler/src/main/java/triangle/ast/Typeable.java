package triangle.ast;


import triangle.types.RuntimeType;

public interface Typeable {

    RuntimeType getType();

    void setType(RuntimeType type);

}
