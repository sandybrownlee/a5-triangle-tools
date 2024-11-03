package triangle.contextualAnalyzer;

import triangle.ast.AllVisitor;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Declaration.ConstDeclaration;
import triangle.ast.Declaration.TypeDeclaration;
import triangle.ast.Expression;
import triangle.ast.Expression.BinaryOp;
import triangle.ast.Expression.FunCall;
import triangle.ast.Expression.Identifier;
import triangle.ast.Expression.Identifier.BasicIdentifier;
import triangle.ast.Expression.IfExpression;
import triangle.ast.Expression.LetExpression;
import triangle.ast.Expression.LitArray;
import triangle.ast.Expression.LitChar;
import triangle.ast.Expression.LitInt;
import triangle.ast.Expression.LitRecord;
import triangle.ast.Expression.UnaryOp;
import triangle.ast.Parameter;
import triangle.ast.Program;
import triangle.ast.Statement;
import triangle.ast.Type;
import triangle.ast.Type.ArrayType;
import triangle.ast.Type.BasicType;
import triangle.ast.Type.RecordType;
import triangle.ast.Type.VoidType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Statefully checks that all uses of indentifiers and types are valid. Uses exceptions for control flow
// TODO: try to accumulate errors instead of failing at the first one
// TODO: refactor to actually use ST typevar of visitors
// TODO: actual polymorphism
// TODO: annotate AST with source code positions
public final class SemanticAnalyzer implements AllVisitor<Void, Type, SemanticAnalyzer.SemanticException> {

    private static final Type                                      binaryRelation =
            new Type.FuncType(List.of(Type.BOOL_TYPE, Type.BOOL_TYPE), Type.BOOL_TYPE);
    private static final Type                                      binaryIntRelation =
            new Type.FuncType(List.of(Type.INT_TYPE, Type.INT_TYPE), Type.BOOL_TYPE);
    private static final Type                                      binaryIntFunc =
            new Type.FuncType(List.of(Type.INT_TYPE, Type.INT_TYPE), Type.INT_TYPE);
    private static final Map<BasicIdentifier, SymbolTable.Binding> STD_TERMS = new HashMap<>();
    public static final  SymbolTable.Env                           STD_ENV = new SymbolTable.Env(
            Map.of(
                    new BasicType("Integer"), Type.INT_TYPE,
                    new BasicType("Char"), Type.CHAR_TYPE,
                    new BasicType("Boolean"), Type.BOOL_TYPE
            ),
            STD_TERMS
    );

    static {
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier("\\/"), new SymbolTable.Binding(binaryRelation, true),
                new BasicIdentifier("/\\"), new SymbolTable.Binding(binaryRelation, true),
                new BasicIdentifier("<="), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(">="), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(">"), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier("<"), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier("\\"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.BOOL_TYPE), Type.BOOL_TYPE), true)
        ));
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier("-"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier("+"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier("*"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier("/"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier("//"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier("|"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.INT_TYPE), Type.INT_TYPE), true),
                new BasicIdentifier("++"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.INT_TYPE), Type.INT_TYPE), true)
        ));
        // these are set to void just as dummy values so that we fail fast in case something tries to access their types since
        //  these are supposed to be special-cased in visit(Expression)
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier("="), new SymbolTable.Binding(Type.VOID_TYPE, true),
                new BasicIdentifier("\\="), new SymbolTable.Binding(Type.VOID_TYPE, true)
        ));
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier("get"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.CHAR_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier("getint"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.INT_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier("geteol"),
                new SymbolTable.Binding(new Type.FuncType(List.of(), Type.VOID_TYPE), true),
                new BasicIdentifier("puteol"),
                new SymbolTable.Binding(new Type.FuncType(List.of(), Type.VOID_TYPE), true),
                new BasicIdentifier("put"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.CHAR_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier("putint"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.INT_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier("chr"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.INT_TYPE), Type.CHAR_TYPE), true),
                new BasicIdentifier("eol"),
                new SymbolTable.Binding(new Type.FuncType(List.of(), Type.BOOL_TYPE), true),
                new BasicIdentifier("ord"),
                new SymbolTable.Binding(new Type.FuncType(List.of(Type.CHAR_TYPE), Type.INT_TYPE), true)
        ));
    }

    private final SymbolTable symtab = new SymbolTable(STD_ENV);

    @Override public Type visit(final Void state, final Argument argument) throws SemanticException {
        return switch (argument) {
            case Argument.FuncArgument(Identifier func) -> {
                Type argType = visit(state, func);
                if (!(argType instanceof Type.FuncType)) {
                    throw new SemanticException.TypeError(argType);
                }

                yield argType;
            }
            case Argument.VarArgument(Identifier var) -> {
                Type argType = visit(state, var);
                if (argType instanceof Type.FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(argType);
                }

                yield argType;
            }
            case Expression expression -> {
                Type argType = visit(state, expression);
                if (argType instanceof Type.FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(argType);
                }

                yield argType;
            }
        };
    }

    @Override public Type visit(final Void state, final Declaration declaration) throws SemanticException {
        return switch (declaration) {
            case ConstDeclaration(String _, Expression value) -> visit(state, value);
            case Declaration.FuncDeclaration(String func, List<Parameter> parameters, Type returnType, Statement statement) -> {
                // resolve the types of the parameters in the current env
                List<Type> resolvedParamTypes = new ArrayList<>();
                for (Parameter parameter : parameters) {
                    Type paramType = visit(state, parameter);
                    resolvedParamTypes.add(paramType);
                }

                // inside the function body
                symtab.enterNewScope();

                // assign each parameter to a basic identifier with its resolved type
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter p = parameters.get(i);
                    symtab.add(new BasicIdentifier(p.getName()), resolvedParamTypes.get(i), p instanceof Parameter.ConstParameter);
                }

                Type resolvedReturnType = visit(state, returnType);

                // (optimistically) assign the function its declared return type
                Type funcType = new Type.FuncType(resolvedParamTypes, resolvedReturnType);
                symtab.add(new BasicIdentifier(func), new Type.FuncType(resolvedParamTypes, resolvedReturnType), true);

                // then type check the function body
                Type statementType = visit(state, statement);

                symtab.exitScope();
                // outside the function

                // if final inferred type is different from declared return type, error
                if (!statementType.equals(resolvedReturnType)) {
                    throw new SemanticException.TypeError(resolvedReturnType, statementType);
                }

                // else our optimistic assumption was right and we can return funcType
                yield funcType;
            }
            case TypeDeclaration typeDeclaration -> visit(state, typeDeclaration.type());
            case Declaration.VarDeclaration varDeclaration -> visit(state, varDeclaration.type());
        };
    }

    @Override public Type visit(final Void state, final Identifier identifier) throws SemanticException {
        return switch (identifier) {
            case Identifier.ArraySubscript(Identifier array, Expression subscript) -> {
                Type arrayType = visit(state, array);
                if (!(arrayType instanceof ArrayType)) {
                    throw new SemanticException.TypeError(arrayType);
                }

                Type subscriptType = visit(state, subscript);
                if (!(subscriptType instanceof Type.IntType)) {
                    throw new SemanticException.TypeError(subscriptType, Type.INT_TYPE);
                }

                yield ((ArrayType) arrayType).elementType();
            }
            case BasicIdentifier basicIdentifier -> {
                Optional<Type> type = symtab.lookup(basicIdentifier);
                if (type.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(basicIdentifier);
                }

                yield type.get();
            }
            case Identifier.RecordAccess(Identifier record, Identifier field) -> {
                Type recordType = visit(state, record);
                if (!(recordType instanceof RecordType)) {
                    throw new SemanticException.TypeError(recordType);
                }

                // record access has a new scope with the field names and types of the record available
                symtab.enterNewScope();
                for (RecordType.RecordFieldType fieldType : ((RecordType) recordType).fieldTypes()) {
                    symtab.add(new BasicIdentifier(fieldType.fieldName()), visit(state, fieldType.fieldType()), true);
                }
                Type fieldType = visit(state, field);
                symtab.exitScope();

                yield fieldType;
            }
        };
    }

    @Override public Type visit(final Void state, final Expression expression) throws SemanticException {
        return switch (expression) {
            case BinaryOp(BasicIdentifier operator, Expression leftOperand, Expression rightOperand) -> {
                Optional<Type> opType = symtab.lookup(operator);

                if (opType.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(operator);
                }

                // I special-case the equality operations because they are the ONLY polymorphic function and I do not have enough
                //  time to implement full polymorphism
                if (operator.name().equals("=")) {
                    Type lOperandType = visit(state, leftOperand);
                    Type rOperandType = visit(state, rightOperand);

                    // just make sure that left and right operands are the same type
                    if (!lOperandType.equals(rOperandType)) {
                        throw new SemanticException.TypeError(lOperandType, rOperandType);
                    }

                    yield Type.BOOL_TYPE;
                }
                if (operator.name().equals("\\=")) {
                    Type lOperandType = visit(state, leftOperand);
                    Type rOperandType = visit(state, rightOperand);

                    // just make sure that left and right operands are the same type
                    if (!lOperandType.equals(rOperandType)) {
                        throw new SemanticException.TypeError(lOperandType, rOperandType);
                    }

                    yield Type.BOOL_TYPE;
                }
                // make sure the above special-case block precedes any attempt to access opType's value, since the equality
                //  operations have their types set to null in the stdenv

                if (!(opType.get() instanceof Type.FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(opType.get());
                }

                if (argTypes.size() != 2) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(operator, 2, argTypes.size());
                }

                Type lOperandType = visit(state, leftOperand);
                Type lOperandExpected = argTypes.get(0);
                if (!lOperandType.equals(lOperandExpected)) {
                    throw new SemanticException.TypeError(lOperandExpected, lOperandType);
                }

                Type rOperandType = visit(state, rightOperand);
                Type rOperandExpected = argTypes.get(1);
                if (!rOperandType.equals(rOperandExpected)) {
                    throw new SemanticException.TypeError(rOperandExpected, rOperandType);
                }

                yield returnType;
            }
            case FunCall(Identifier callable, List<Argument> arguments) -> {
                Type funcType = visit(state, callable);

                if (!(funcType instanceof Type.FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(funcType);
                }

                if (argTypes.size() != arguments.size()) {
                    throw new SemanticException.ArityMismatch(callable, argTypes.size(), arguments.size());
                }

                for (int i = 0; i < argTypes.size(); i++) {
                    Type argType = visit(state, arguments.get(i));
                    Type expectedType = visit(state, argTypes.get(i));
                    if (!(argType.equals(expectedType))) {
                        throw new SemanticException.TypeError(argType, expectedType);
                    }
                }

                yield returnType;
            }
            case Identifier identifier -> visit(state, identifier);
            case IfExpression(Expression condition, Expression consequent, Expression alternative) -> {
                Type condType = visit(state, condition);
                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                Type consequentType = visit(state, consequent);
                Type alternativeType = visit(state, alternative);
                if (!(consequentType.equals(alternativeType))) {
                    throw new SemanticException.TypeError(consequentType, alternativeType);
                }

                yield consequentType;
            }
            case LetExpression(List<Declaration> declarations, Expression letExpression) -> {
                symtab.enterNewScope();

                for (Declaration declaration : declarations) {
                    Type type = visit(state, declaration);
                    symtab.add(new BasicIdentifier(declaration.getName()), type, declaration instanceof ConstDeclaration);
                }
                Type exprType = visit(state, letExpression);

                symtab.exitScope();
                yield exprType;
            }
            case LitArray(List<Expression> values) -> {
                if (values.isEmpty()) {
                    // type of empty array?
                    throw new UnsupportedOperationException();
                }

                Type expectedType = visit(state, values.getFirst());
                for (Expression value : values) {
                    Type elementType = visit(state, value);
                    if (!elementType.equals(expectedType)) {
                        throw new SemanticException.TypeError(expectedType, elementType);
                    }
                }

                yield new ArrayType(values.size(), expectedType);
            }
            case LitChar _ -> Type.CHAR_TYPE;
            case LitInt _ -> Type.INT_TYPE;
            case LitRecord(List<LitRecord.RecordField> fields) -> {
                if (fields.isEmpty()) {
                    // implementing this sensibly will take row-poly types which is too hard so we just treat all empty records
                    // as belonging to a single type
                    yield new RecordType(Collections.emptyList());
                }

                List<RecordType.RecordFieldType> fieldTypes = new ArrayList<>(fields.size());
                for (LitRecord.RecordField field : fields) {
                    fieldTypes.add(new RecordType.RecordFieldType(field.name(), visit(state, field.value())));
                }

                yield new RecordType(fieldTypes);
            }
            case UnaryOp(BasicIdentifier operator, Expression operand) -> {
                Optional<Type> opType = symtab.lookup(operator);

                if (opType.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(operator);
                }

                if (!(opType.get() instanceof Type.FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(opType.get());
                }

                if (argTypes.size() != 1) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(operator, 1, argTypes.size());
                }

                Type operandType = visit(state, operand);
                Type operandExpected = argTypes.getFirst();
                if (!operandType.equals(operandExpected)) {
                    throw new SemanticException.TypeError(operandExpected, operandType);
                }

                yield returnType;
            }
            case Expression.LitBool _ -> Type.BOOL_TYPE;
        };
    }

    @Override public Type visit(final Void state, final Parameter parameter) throws SemanticException {
        return switch (parameter) {
            case Parameter.FuncParameter(String _, List<Parameter> parameters, Type returnType) -> {
                List<Type> paramTypes = new ArrayList<>();
                for (Parameter p : parameters) {
                    paramTypes.add(visit(state, p));
                }

                yield new Type.FuncType(paramTypes, visit(state, returnType));
            }

            case Parameter.VarParameter(String _, Type type) -> visit(state, type);
            case Parameter.ConstParameter(String _, Type type) -> visit(state, type);
        };
    }

    @Override public Type visit(final Void state, final Program program) throws SemanticException {
        for (final Statement statement : program.statements()) {
            visit(state, statement);
        }
        return Type.VOID_TYPE;
    }

    @Override public Type visit(final Void state, final Statement statement) throws SemanticException {
        switch (statement) {
            case Expression expression -> {
                // Expressions are the only statements that have a non-void type
                return visit(state, expression);
            }
            case Statement.AssignStatement assignStatement -> {
                Type lType = visit(state, assignStatement.identifier());
                Type rType = visit(state, assignStatement.expression());

                if (!lType.equals(rType)) {
                    throw new SemanticException.TypeError(lType, rType);
                }

                if (symtab.isConstant(Identifier.getRoot(assignStatement.identifier()))) {
                    throw new SemanticException.AssignmentToConstant(assignStatement.identifier());
                }
            }
            case Statement.IfStatement ifStatement -> {
                Type condType = visit(state, ifStatement.condition());

                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                if (ifStatement.consequent().isPresent()) {
                    visit(state, ifStatement.consequent().get());
                }

                if (ifStatement.alternative().isPresent()) {
                    visit(state, ifStatement.alternative().get());
                }
            }
            case Statement.LetStatement(List<Declaration> declarations, Statement letStatement) -> {
                symtab.enterNewScope();

                for (Declaration declaration : declarations) {
                    switch (declaration) {
                        case TypeDeclaration _ -> symtab.add(new BasicType(declaration.getName()), visit(state, declaration));
                        case ConstDeclaration _, Declaration.FuncDeclaration _, Declaration.VarDeclaration _ -> symtab.add(
                                new BasicIdentifier(declaration.getName()), visit(state, declaration),
                                declaration instanceof ConstDeclaration);
                    }
                }

                visit(state, letStatement);
                symtab.exitScope();
            }
            case Statement.LoopWhileStatement(Expression condition, Statement loopBody, Statement doBody) -> {
                Type condType = visit(state, condition);

                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                visit(state, loopBody);
                visit(state, doBody);
            }
            case Statement.RepeatUntilStatement(Expression condition, Statement body) -> {
                Type condType = visit(state, condition);
                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                visit(state, body);
            }
            case Statement.RepeatWhileStatement(Expression condition, Statement body) -> {
                Type condType = visit(state, condition);
                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                visit(state, body);
            }
            case Statement.StatementBlock(List<Statement> statements) -> {
                for (Statement stmt : statements) {
                    visit(state, stmt);
                }
            }
            case Statement.WhileStatement(Expression condition, Statement body) -> {
                Type condType = visit(state, condition);
                if (!(condType instanceof Type.BoolType)) {
                    throw new SemanticException.TypeError(condType, Type.BOOL_TYPE);
                }

                visit(state, body);
            }
        }

        // all statements expect expressions return Void
        return Type.VOID_TYPE;
    }

    @Override public Type visit(final Void state, final Type type) throws SemanticException {
        return switch (type) {
            case ArrayType(int size, Type elementType) -> new ArrayType(size, visit(state, elementType));
            case RecordType recordType -> {
                List<RecordType.RecordFieldType> resolvedFieldTypes = new ArrayList<>();
                for (RecordType.RecordFieldType fieldType : recordType.fieldTypes()) {
                    resolvedFieldTypes.add(
                            new RecordType.RecordFieldType(fieldType.fieldName(), visit(state, fieldType.fieldType())));
                }

                yield new RecordType(resolvedFieldTypes);
            }
            case BasicType basicType -> {
                Optional<Type> typeLookup = symtab.lookup(basicType);

                if (typeLookup.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(basicType);
                }

                yield visit(state, typeLookup.get());
            }
            case VoidType voidType -> voidType;
            case Type.BoolType boolType -> boolType;
            case Type.CharType charType -> charType;
            case Type.IntType intType -> intType;
            case Type.FuncType(List<Type> argTypes, Type returnType) -> {
                List<Type> resolvedArgTypes = new ArrayList<>();
                for (Type argType : argTypes) {
                    resolvedArgTypes.add(visit(state, argType));
                }

                yield new Type.FuncType(resolvedArgTypes, visit(state, returnType));
            }
        };
    }

    private static final class SymbolTable {

        private final Deque<Env> scopes;

        private SymbolTable(Env initEnv) {
            scopes = new ArrayDeque<>();
            scopes.push(initEnv);
        }

        private Optional<Type> lookup(BasicIdentifier identifier) {
            for (Env env : scopes) {
                if (env.terms.containsKey(identifier)) {
                    return Optional.of(env.terms.get(identifier).type);
                }
            }

            return Optional.empty();
        }

        private Optional<Binding> lookupBinding(BasicIdentifier identifier) {
            for (Env env : scopes) {
                if (env.terms.containsKey(identifier)) {
                    return Optional.of(env.terms.get(identifier));
                }
            }

            return Optional.empty();
        }

        private Optional<Type> lookup(BasicType type) {
            for (Env env : scopes) {
                if (env.types.containsKey(type)) {
                    return Optional.of(env.types.get(type));
                }
            }

            return Optional.empty();
        }

        private void add(BasicIdentifier identifier, Type type, boolean constant) {
            scopes.peek().terms.put(identifier, new Binding(type, constant));
        }

        private void add(BasicType basicType, Type type) {
            scopes.peek().types.put(basicType, type);
        }

        private boolean isConstant(BasicIdentifier identifier) {
            return lookupBinding(identifier).get().constant;
        }

        private void enterNewScope() {
            scopes.push(new Env(new HashMap<>(), new HashMap<>()));
        }

        private void exitScope() {
            scopes.pop();
        }

        private record Binding(Type type, boolean constant) { }

        public record Env(Map<BasicType, Type> types, Map<BasicIdentifier, Binding> terms) { }

    }

    public static abstract class SemanticException extends Exception {

        public SemanticException(final String s) {
            super(s);
        }

        private static final class UndeclaredUse extends SemanticException {

            private UndeclaredUse(Identifier identifier) {
                super("Undeclared use of identifier: " + identifier);
            }

            private UndeclaredUse(Type type) {
                super("Undeclared use of type: " + type);
            }

        }

        private static final class TypeError extends SemanticException {

            private TypeError(Type left, Type right) {
                super("Type mismatch: " + left + " got: " + right);
            }

            private TypeError(Type type) {
                super("Unexpected type: " + type);
            }

        }

        private static final class ArityMismatch extends SemanticException {

            private ArityMismatch(Expression callExpression, int expectedArity, int arity) {
                super("Arity mismatch: " + callExpression + " expected: " + expectedArity + " got: " + arity);
            }

        }

        private static final class AssignmentToConstant extends SemanticException {

            private AssignmentToConstant(final Identifier identifier) {
                super("Assignment to constant: " + identifier);
            }

        }

    }

}
