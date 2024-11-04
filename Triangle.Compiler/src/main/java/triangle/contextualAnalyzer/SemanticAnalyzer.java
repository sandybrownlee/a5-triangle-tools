package triangle.contextualAnalyzer;

import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Declaration.ConstDeclaration;
import triangle.ast.Declaration.TypeDeclaration;
import triangle.ast.Expression;
import triangle.ast.Expression.*;
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
import triangle.ast.Type.PrimType.VoidType;

import static triangle.ast.Type.BOOL_TYPE;
import static triangle.ast.Type.INT_TYPE;

import triangle.ast.Type.PrimType.FuncType;
import triangle.syntacticAnalyzer.SourcePosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SemanticAnalyzer {

    private static final Type                                      binaryRelation =
            new FuncType(List.of(BOOL_TYPE, BOOL_TYPE), BOOL_TYPE);
    private static final Type                                      binaryIntRelation =
            new FuncType(List.of(INT_TYPE, INT_TYPE), BOOL_TYPE);
    private static final Type                                      binaryIntFunc =
            new FuncType(List.of(INT_TYPE, INT_TYPE), INT_TYPE);
    private static final Map<BasicIdentifier, SymbolTable.Binding> STD_TERMS = new HashMap<>();
    private static final Map<BasicType, Type>                      STD_TYPES = Map.of(
            new BasicType("Integer"), INT_TYPE,
            new BasicType("Char"), Type.CHAR_TYPE,
            new BasicType("Boolean"), BOOL_TYPE
    );

    // stdenv values have null as annotation
    static {
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier(null, "\\/"), new SymbolTable.Binding(binaryRelation, true),
                new BasicIdentifier(null, "/\\"), new SymbolTable.Binding(binaryRelation, true),
                new BasicIdentifier(null, "<="), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(null, ">="), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(null, ">"), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(null, "<"), new SymbolTable.Binding(binaryIntRelation, true),
                new BasicIdentifier(null, "\\"),
                new SymbolTable.Binding(new FuncType(List.of(BOOL_TYPE), BOOL_TYPE), true)
        ));
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier(null, "-"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier(null, "+"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier(null, "*"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier(null, "/"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier(null, "//"), new SymbolTable.Binding(binaryIntFunc, true),
                new BasicIdentifier(null, "|"),
                new SymbolTable.Binding(new FuncType(List.of(INT_TYPE), INT_TYPE), true),
                new BasicIdentifier(null, "++"),
                new SymbolTable.Binding(new FuncType(List.of(INT_TYPE), INT_TYPE), true)
        ));
        // these are set to void just as dummy values so that we fail fast in case something tries to access their types since
        //  these are supposed to be special-cased in analyze(Expression)
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier(null, "="), new SymbolTable.Binding(Type.VOID_TYPE, true),
                new BasicIdentifier(null, "\\="), new SymbolTable.Binding(Type.VOID_TYPE, true)
        ));
        STD_TERMS.putAll(Map.of(
                new BasicIdentifier(null, "get"),
                new SymbolTable.Binding(new FuncType(List.of(Type.CHAR_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "getint"),
                new SymbolTable.Binding(new FuncType(List.of(INT_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "geteol"),
                new SymbolTable.Binding(new FuncType(List.of(), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "puteol"),
                new SymbolTable.Binding(new FuncType(List.of(), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "put"),
                new SymbolTable.Binding(new FuncType(List.of(Type.CHAR_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "putint"),
                new SymbolTable.Binding(new FuncType(List.of(INT_TYPE), Type.VOID_TYPE), true),
                new BasicIdentifier(null, "chr"),
                new SymbolTable.Binding(new FuncType(List.of(INT_TYPE), Type.CHAR_TYPE), true),
                new BasicIdentifier(null, "eol"),
                new SymbolTable.Binding(new FuncType(List.of(), BOOL_TYPE), true),
                new BasicIdentifier(null, "ord"),
                new SymbolTable.Binding(new FuncType(List.of(Type.CHAR_TYPE), INT_TYPE), true)
        ));
    }

    private final SymbolTable symtab = new SymbolTable(STD_TYPES, STD_TERMS);

    public Type analyze(final Program program) throws SemanticException {
        for (final Statement statement : program.statements()) {
            analyze(statement);
        }
        return Type.VOID_TYPE;
    }

    private Type analyze(final Argument argument) throws SemanticException {
        return switch (argument) {
            case Argument.FuncArgument(_, Identifier func) -> {
                Type argType = analyze(func);
                if (!(argType instanceof FuncType)) {
                    throw new SemanticException.TypeError(argType, "function");
                }

                yield argType;
            }
            case Argument.VarArgument(_, Identifier var) -> {
                Type argType = analyze(var);
                if (argType instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(argType, "not a function");
                }

                yield argType;
            }
            case Expression expression -> {
                Type argType = analyze(expression);
                if (argType instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(argType, "not a function");
                }

                yield argType;
            }
        };
    }

    // needs to rethrow DuplicateRecordTypeField after annotating it with sourcePos info
    private Type analyze(final Declaration declaration) throws SemanticException {
        return switch (declaration) {
            case ConstDeclaration(SourcePosition sourcePos, String _, Expression value) -> {
                try {
                    yield analyze(value);
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(sourcePos, e.getFieldType());
                }
            }
            case Declaration.FuncDeclaration(
                    SourcePosition sourcePos, String func, List<Parameter> parameters, Type returnType,
                    Statement statement
            ) -> {
                // check for duplicate parameter
                Set<String> seenParameters = new HashSet<>();
                for (Parameter param : parameters) {
                    if (seenParameters.contains(param.getName())) {
                        throw new SemanticException.DuplicateParameter(sourcePos, param);
                    }

                    seenParameters.add(param.getName());
                }

                // resolve the types of the parameters in the current env
                List<Type> resolvedParamTypes = new ArrayList<>();
                for (Parameter parameter : parameters) {
                    Type paramType = analyze(parameter);
                    resolvedParamTypes.add(paramType);
                }

                // inside the function body
                symtab.enterNewScope();

                // assign each parameter to a basic identifier with its resolved type
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter p = parameters.get(i);
                    symtab.add(new BasicIdentifier(p.sourcePos(), p.getName()), resolvedParamTypes.get(i),
                               p instanceof Parameter.ConstParameter
                    );
                }

                Type resolvedReturnType;
                try {
                    resolvedReturnType = analyze(returnType);
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(sourcePos, e.getFieldType());
                }

                // (optimistically) assign the function its declared return type
                Type funcType = new FuncType(resolvedParamTypes, resolvedReturnType);
                symtab.add(new BasicIdentifier(sourcePos, func), new FuncType(resolvedParamTypes, resolvedReturnType),
                           true
                );

                // then type check the function body
                Type statementType = analyze(statement);

                symtab.exitScope();
                // outside the function

                // if final inferred type is different from declared return type, error
                if (!statementType.equals(resolvedReturnType)) {
                    throw new SemanticException.TypeError(resolvedReturnType, statementType);
                }

                // else our optimistic assumption was right and we can return funcType
                yield funcType;
            }
            case TypeDeclaration typeDeclaration -> {
                try {
                    yield analyze(typeDeclaration.type());
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(typeDeclaration.sourcePos(), e.getFieldType());
                }
            }
            case Declaration.VarDeclaration varDeclaration -> {
                try {
                    yield analyze(varDeclaration.type());
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(varDeclaration.sourcePos(), e.getFieldType());
                }
            }
        };
    }

    private Type analyze(final Identifier identifier) throws SemanticException {
        return switch (identifier) {
            case Identifier.ArraySubscript(_, Identifier array, Expression subscript) -> {
                Type arrayType = analyze(array);
                if (!(arrayType instanceof ArrayType)) {
                    throw new SemanticException.TypeError(arrayType, "array");
                }

                Type subscriptType = analyze(subscript);
                if (!(subscriptType instanceof Type.PrimType.IntType)) {
                    throw new SemanticException.TypeError(subscriptType, INT_TYPE);
                }

                yield ((ArrayType) arrayType).elementType();
            }
            case BasicIdentifier basicIdentifier -> {
                Optional<SymbolTable.Binding> binding = symtab.lookup(basicIdentifier);
                if (binding.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(basicIdentifier.sourcePos(), basicIdentifier);
                }

                yield binding.get().type();
            }
            case Identifier.RecordAccess(_, Identifier record, Identifier field) -> {
                Type recordType = analyze(record);
                if (!(recordType instanceof RecordType)) {
                    throw new SemanticException.TypeError(recordType, "record");
                }

                // record access has a new scope with the field names and types of the record available
                symtab.enterNewScope();
                for (RecordType.RecordFieldType fieldType : ((RecordType) recordType).fieldTypes()) {
                    symtab.add(new BasicIdentifier(null, fieldType.fieldName()), analyze(fieldType.fieldType()), true);
                }
                Type fieldType = analyze(field);
                symtab.exitScope();

                yield fieldType;
            }
        };
    }

    private Type analyze(final Expression expression) throws SemanticException {
        return switch (expression) {
            case BinaryOp(
                    SourcePosition sourcePos, BasicIdentifier operator, Expression leftOperand, Expression rightOperand
            ) -> {
                Optional<SymbolTable.Binding> opBinding = symtab.lookup(operator);
                if (opBinding.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(sourcePos, operator);
                }

                Type opType = opBinding.get().type();

                // I special-case the equality operations because they are the ONLY polymorphic function and I do not have enough
                //  time to implement full polymorphism
                if (operator.name().equals("=")) {
                    Type lOperandType = analyze(leftOperand);
                    Type rOperandType = analyze(rightOperand);

                    // just make sure that left and right operands are the same type
                    if (!lOperandType.equals(rOperandType)) {
                        throw new SemanticException.TypeError(lOperandType, rOperandType);
                    }

                    yield BOOL_TYPE;
                }
                if (operator.name().equals("\\=")) {
                    Type lOperandType = analyze(leftOperand);
                    Type rOperandType = analyze(rightOperand);

                    // just make sure that left and right operands are the same type
                    if (!lOperandType.equals(rOperandType)) {
                        throw new SemanticException.TypeError(lOperandType, rOperandType);
                    }

                    yield BOOL_TYPE;
                }
                // make sure the above special-case block precedes any attempt to access opType's value, since the equality
                //  operations have their types set to null in the stdenv

                if (!(opType instanceof FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(opType, "function");
                }

                if (argTypes.size() != 2) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(sourcePos, operator, 2, argTypes.size());
                }

                Type lOperandType = analyze(leftOperand);
                Type lOperandExpected = argTypes.get(0);
                if (!lOperandType.equals(lOperandExpected)) {
                    throw new SemanticException.TypeError(lOperandExpected, lOperandType);
                }

                Type rOperandType = analyze(rightOperand);
                Type rOperandExpected = argTypes.get(1);
                if (!rOperandType.equals(rOperandExpected)) {
                    throw new SemanticException.TypeError(rOperandExpected, rOperandType);
                }

                yield returnType;
            }
            case FunCall(SourcePosition sourcePos, Identifier callable, List<Argument> arguments) -> {
                Type funcType = analyze(callable);

                if (!(funcType instanceof FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(funcType, "function");
                }

                if (argTypes.size() != arguments.size()) {
                    throw new SemanticException.ArityMismatch(sourcePos, callable, argTypes.size(), arguments.size());
                }

                for (int i = 0; i < argTypes.size(); i++) {
                    Type argType = analyze(arguments.get(i));
                    Type expectedType = analyze(argTypes.get(i));
                    if (!(argType.equals(expectedType))) {
                        throw new SemanticException.TypeError(argType, expectedType);
                    }
                }

                yield returnType;
            }
            case Identifier identifier -> analyze(identifier);
            case IfExpression(_, Expression condition, Expression consequent, Expression alternative) -> {
                Type condType = analyze(condition);
                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                Type consequentType = analyze(consequent);
                Type alternativeType = analyze(alternative);
                if (!(consequentType.equals(alternativeType))) {
                    throw new SemanticException.TypeError(consequentType, alternativeType);
                }

                yield consequentType;
            }
            case LetExpression(_, List<Declaration> declarations, Expression letExpression) -> {
                symtab.enterNewScope();

                for (Declaration declaration : declarations) {
                    Type type = analyze(declaration);
                    symtab.add(new BasicIdentifier(declaration.sourcePos(), declaration.getName()), type,
                               declaration instanceof ConstDeclaration
                    );
                }
                Type exprType = analyze(letExpression);

                symtab.exitScope();
                yield exprType;
            }
            case LitArray(_, List<Expression> values) -> {
                if (values.isEmpty()) {
                    // type of empty array?
                    throw new UnsupportedOperationException();
                }

                Type expectedType = analyze(values.getFirst());
                for (Expression value : values) {
                    Type elementType = analyze(value);
                    if (!elementType.equals(expectedType)) {
                        throw new SemanticException.TypeError(expectedType, elementType);
                    }
                }

                yield new ArrayType(values.size(), expectedType);
            }
            case LitChar _ -> Type.CHAR_TYPE;
            case LitInt _ -> INT_TYPE;
            case LitRecord(SourcePosition sourcePos, List<LitRecord.RecordField> fields) -> {
                if (fields.isEmpty()) {
                    // implementing this sensibly will take row-poly types which is too hard so we just treat all empty records
                    // as belonging to a single type
                    yield new RecordType(Collections.emptyList());
                }

                List<RecordType.RecordFieldType> fieldTypes = new ArrayList<>(fields.size());
                Set<String> seenFieldNames = new HashSet<>();
                for (LitRecord.RecordField field : fields) {
                    if (seenFieldNames.contains(field.name())) {
                        throw new SemanticException.DuplicateRecordField(sourcePos, field);
                    }
                    seenFieldNames.add(field.name());
                    fieldTypes.add(new RecordType.RecordFieldType(field.name(), analyze(field.value())));
                }

                yield new RecordType(fieldTypes);
            }
            case UnaryOp(SourcePosition sourcePos, BasicIdentifier operator, Expression operand) -> {
                Optional<SymbolTable.Binding> opBinding = symtab.lookup(operator);
                if (opBinding.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(sourcePos, operator);
                }

                Type opType = opBinding.get().type();

                if (!(opType instanceof FuncType(List<Type> argTypes, Type returnType))) {
                    throw new SemanticException.TypeError(opType, "function");
                }

                if (argTypes.size() != 1) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(sourcePos, operator, 1, argTypes.size());
                }

                Type operandType = analyze(operand);
                Type operandExpected = argTypes.getFirst();
                if (!operandType.equals(operandExpected)) {
                    throw new SemanticException.TypeError(operandExpected, operandType);
                }

                yield returnType;
            }
            case Expression.LitBool _ -> BOOL_TYPE;
        };
    }

    private Type analyze(final Parameter parameter) throws SemanticException {
        return switch (parameter) {
            case Parameter.FuncParameter(_, String _, List<Parameter> parameters, Type returnType) -> {
                List<Type> paramTypes = new ArrayList<>();
                for (Parameter p : parameters) {
                    paramTypes.add(analyze(p));
                }

                yield new FuncType(paramTypes, analyze(returnType));
            }

            case Parameter.VarParameter(_, String _, Type type) -> analyze(type);
            case Parameter.ConstParameter(_, String _, Type type) -> analyze(type);
        };
    }

    private Type analyze(final Statement statement) throws SemanticException {
        switch (statement) {
            case Expression expression -> {
                // Expressions are the only statements that have a non-void type
                return analyze(expression);
            }
            case Statement.AssignStatement(SourcePosition sourcePos, Identifier lvalue, Expression rvalue) -> {
                Type lType = analyze(lvalue);
                Type rType = analyze(rvalue);

                if (!lType.equals(rType)) {
                    throw new SemanticException.TypeError(lType, rType);
                }

                if (symtab.isConstant(lvalue.root())) {
                    throw new SemanticException.AssignmentToConstant(sourcePos, lvalue);
                }
            }
            case Statement.IfStatement ifStatement -> {
                Type condType = analyze(ifStatement.condition());

                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                if (ifStatement.consequent().isPresent()) {
                    analyze(ifStatement.consequent().get());
                }

                if (ifStatement.alternative().isPresent()) {
                    analyze(ifStatement.alternative().get());
                }
            }
            case Statement.LetStatement(_, List<Declaration> declarations, Statement letStatement) -> {
                symtab.enterNewScope();

                for (Declaration declaration : declarations) {
                    switch (declaration) {
                        case TypeDeclaration _ -> symtab.add(new BasicType(declaration.getName()), analyze(declaration));
                        case ConstDeclaration _, Declaration.FuncDeclaration _, Declaration.VarDeclaration _ -> symtab.add(
                                new BasicIdentifier(declaration.sourcePos(), declaration.getName()), analyze(declaration),
                                declaration instanceof ConstDeclaration
                        );
                    }
                }

                analyze(letStatement);
                symtab.exitScope();
            }
            case Statement.LoopWhileStatement(_, Expression condition, Statement loopBody, Statement doBody) -> {
                Type condType = analyze(condition);

                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                analyze(loopBody);
                analyze(doBody);
            }
            case Statement.RepeatUntilStatement(_, Expression condition, Statement body) -> {
                Type condType = analyze(condition);
                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                analyze(body);
            }
            case Statement.RepeatWhileStatement(_, Expression condition, Statement body) -> {
                Type condType = analyze(condition);
                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                analyze(body);
            }
            case Statement.StatementBlock(_, List<Statement> statements) -> {
                for (Statement stmt : statements) {
                    analyze(stmt);
                }
            }
            case Statement.WhileStatement(_, Expression condition, Statement body) -> {
                Type condType = analyze(condition);
                if (!(condType instanceof Type.PrimType.BoolType)) {
                    throw new SemanticException.TypeError(condType, BOOL_TYPE);
                }

                analyze(body);
            }
        }

        // all statements expect expressions return Void
        return Type.VOID_TYPE;
    }

    private Type analyze(final Type type) throws SemanticException {
        return switch (type) {
            case ArrayType(int size, Type elementType) -> new ArrayType(size, analyze(elementType));
            case RecordType recordType -> {
                // check for duplicate fields in the record type definition
                Set<String> seenFieldNames = new HashSet<>();
                for (RecordType.RecordFieldType field : recordType.fieldTypes()) {
                    if (seenFieldNames.contains(field.fieldName())) {
                        throw new SemanticException.DuplicateRecordTypeField(field);
                    }

                    seenFieldNames.add(field.fieldName());
                }

                List<RecordType.RecordFieldType> resolvedFieldTypes = new ArrayList<>();
                for (RecordType.RecordFieldType fieldType : recordType.fieldTypes()) {
                    resolvedFieldTypes.add(
                            new RecordType.RecordFieldType(fieldType.fieldName(), analyze(fieldType.fieldType())));
                }

                yield new RecordType(resolvedFieldTypes);
            }
            case BasicType basicType -> {
                Optional<Type> typeLookup = symtab.lookup(basicType);

                if (typeLookup.isEmpty()) {
                    throw new SemanticException.UndeclaredUse(basicType);
                }

                yield analyze(typeLookup.get());
            }
            case VoidType voidType -> voidType;
            case Type.PrimType.BoolType boolType -> boolType;
            case Type.PrimType.CharType charType -> charType;
            case Type.PrimType.IntType intType -> intType;
            case FuncType(List<Type> argTypes, Type returnType) -> {
                List<Type> resolvedArgTypes = new ArrayList<>();
                for (Type argType : argTypes) {
                    resolvedArgTypes.add(analyze(argType));
                }

                yield new FuncType(resolvedArgTypes, analyze(returnType));
            }
        };
    }

}
