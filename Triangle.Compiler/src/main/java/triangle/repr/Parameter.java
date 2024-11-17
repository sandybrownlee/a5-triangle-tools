package triangle.repr;

import java.util.List;

sealed public abstract class Parameter implements Annotatable.Typeable, Annotatable.SourceLocatable {

    protected SourcePosition sourcePos;
    protected Type           type;
    protected String         name;

    @Override public Type getType() {
        return type;
    }

    @Override public void setType(Type type) {
        this.type = type;
    }

    @Override public void setSourcePosition(final SourcePosition sourcePos) {
        this.sourcePos = sourcePos;
    }

    @Override public SourcePosition sourcePosition() {
        return sourcePos;
    }

    public String name() {
        return name;
    }

    public static final class ValueParameter extends Parameter {

        private final TypeSig typeSig;

        public ValueParameter(String name, TypeSig typeSig) {
            this.name = name;
            this.typeSig = typeSig;
        }

        public TypeSig declaredType() {
            return typeSig;
        }

    }

    public static final class VarParameter extends Parameter {

        private final TypeSig typeSig;

        public VarParameter(String name, TypeSig typeSig) {
            this.name = name;
            this.typeSig = typeSig;
        }

        public TypeSig declaredType() {
            return typeSig;
        }

    }

    public static final class FuncParameter extends Parameter {

        private final List<Parameter> parameters;
        private final TypeSig         returnTypeSig;

        public FuncParameter(String name, List<Parameter> parameters, TypeSig returnTypeSig) {
            this.name = name;
            this.parameters = parameters;
            this.returnTypeSig = returnTypeSig;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public TypeSig declaredReturnType() {
            return returnTypeSig;
        }

    }

}
