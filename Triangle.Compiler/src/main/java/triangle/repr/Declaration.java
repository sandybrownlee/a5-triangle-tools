package triangle.repr;

import java.util.List;

sealed public abstract class Declaration implements Annotatable.SourceLocatable {

    protected SourcePosition sourcePos;

    @Override public final SourcePosition sourcePosition() {
        return sourcePos;
    }

    @Override public final void setSourcePosition(final SourcePosition sourcePos) {
        this.sourcePos = sourcePos;
    }

    public static final class ConstDeclaration extends Declaration {

        private final String     name;
        private final Expression value;

        public ConstDeclaration(String name, Expression value) {
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public Expression value() {
            return value;
        }

    }

    public static final class VarDeclaration extends Declaration implements Typeable {

        private final String  name;
        private final TypeSig declaredType;
        private       Type    type;

        public VarDeclaration(String name, TypeSig declaredType) {
            this.name = name;
            this.declaredType = declaredType;
        }

        @Override public Type getType() {
            return type;
        }

        @Override public void setType(final Type type) {
            this.type = type;
        }

        public String name() {
            return name;
        }

        public TypeSig declaredType() {
            return this.declaredType;
        }

    }

    public static final class FuncDeclaration extends Declaration {

        private final String          name;
        private final List<Parameter> parameters;
        private final TypeSig         returnTypeSig;
        private final Expression      expression;

        public FuncDeclaration(
                String name, List<Parameter> parameters, TypeSig returnTypeSig, Expression expression
        ) {
            this.name = name;
            this.parameters = parameters;
            this.returnTypeSig = returnTypeSig;
            this.expression = expression;
        }

        public String name() {
            return name;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public TypeSig returnTypeSig() {
            return returnTypeSig;
        }

        public Expression expression() {
            return expression;
        }

    }

    public static final class ProcDeclaration extends Declaration {

        private final String          name;
        private final List<Parameter> parameters;
        private final Statement       statement;

        public ProcDeclaration(String name, List<Parameter> parameters, Statement statement) {
            this.name = name;
            this.parameters = parameters;
            this.statement = statement;
        }

        public String name() {
            return name;
        }

        public List<Parameter> parameters() {
            return parameters;
        }

        public Statement statement() {
            return statement;
        }

    }

    public static final class TypeDeclaration extends Declaration {

        private final String  name;
        private final TypeSig typeSig;

        public TypeDeclaration(String name, TypeSig typeSig) {
            this.name = name;
            this.typeSig = typeSig;
        }

        public String name() {
            return name;
        }

        public TypeSig typeSig() {
            return typeSig;
        }

    }

}
