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
import triangle.ast.Statement;
import triangle.ast.Type;
import triangle.types.RuntimeType;
import triangle.types.RuntimeType.ArrayType;
import triangle.types.RuntimeType.RecordType;

import triangle.types.RuntimeType.PrimType.FuncType;
import triangle.ast.SourcePosition;
import triangle.util.SymbolTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static triangle.ast.Type.BasicType;
import static triangle.types.RuntimeType.*;

// semantic analysis does several things:
//      it checks that all uses of an identifier are in a scope with a corresponding declaration
//      it updates the AST to annotated all uses of an identifier with its corresponding declaration
//      it checks that all expressions have the correct type for how they are being used
//      it updates the AST to annotated all Typeable nodes with their types; i.e., it produces an explicitly-typed AST
//      it ensures that the program does not attempt to return a function/procedure as a value, as HOF are not supported

// WARNING: this class uses exception as control flow; this is to allow checking to resume from a known "safe point"
public final class SemanticAnalyzer {

    //@formatter:off
    private static final RuntimeType          binaryRelation    = new FuncType(List.of(BOOL_TYPE, BOOL_TYPE), BOOL_TYPE);
    private static final RuntimeType          binaryIntRelation = new FuncType(List.of(INT_TYPE, INT_TYPE), BOOL_TYPE);
    private static final RuntimeType          binaryIntFunc     = new FuncType(List.of(INT_TYPE, INT_TYPE), INT_TYPE);
    private static final Map<String, Binding> STD_TERMS         = new HashMap<>();
    private static final Map<String, RuntimeType>    STD_TYPES =
            Map.of(
                    "Integer", INT_TYPE,
                    "Char", CHAR_TYPE,
                    "Boolean",BOOL_TYPE);

    // stdenv values have null as annotation
    static {
        // TODO: this is very hackish and ugly
        STD_TERMS.putAll(
                Map.of(
                    "\\/", new Binding(binaryRelation, true, null),
                    "/\\", new Binding(binaryRelation, true, null),
                    "<=", new Binding(binaryIntRelation, true, null),
                    ">=", new Binding(binaryIntRelation, true, null),
                    ">", new Binding(binaryIntRelation, true, null),
                    "<", new Binding(binaryIntRelation, true, null),
                    "\\", new Binding(new FuncType(List.of(BOOL_TYPE), BOOL_TYPE), true, null)));

        STD_TERMS.putAll(
                Map.of(
                    "-", new Binding(binaryIntFunc, true, null),
                    "+", new Binding(binaryIntFunc, true, null),
                    "*", new Binding(binaryIntFunc, true, null),
                    "/", new Binding(binaryIntFunc, true, null),
                    "//", new Binding(binaryIntFunc, true, null),
                    "|", new Binding(new FuncType(List.of(INT_TYPE), INT_TYPE), true, null),
                    "++", new Binding(new FuncType(List.of(INT_TYPE), INT_TYPE), true, null)));

        // these are set to void just as dummy values so that we fail fast in case something tries to access their types since
        //  these are supposed to be special-cased in analyze(Expression)
        STD_TERMS.putAll(
                Map.of(
                        "=", new Binding(VOID_TYPE, true, null),
                        "\\=", new Binding(VOID_TYPE, true, null)));

        STD_TERMS.putAll(
                Map.of(
                        "get", new Binding(new FuncType(List.of(CHAR_TYPE), VOID_TYPE), true, null),
                        "getint", new Binding(new FuncType(List.of(INT_TYPE), VOID_TYPE), true, null),
                        "geteol", new Binding(new FuncType(List.of(), VOID_TYPE), true, null),
                        "puteol", new Binding(new FuncType(List.of(), VOID_TYPE), true, null),
                        "put", new Binding(new FuncType(List.of(CHAR_TYPE), VOID_TYPE), true, null),
                        "putint", new Binding(new FuncType(List.of(INT_TYPE), VOID_TYPE), true, null),
                        "chr", new Binding(new FuncType(List.of(INT_TYPE), CHAR_TYPE), true, null),
                        "eol", new Binding(new FuncType(List.of(), BOOL_TYPE), true, null),
                        "ord", new Binding(new FuncType(List.of(CHAR_TYPE), INT_TYPE), true, null)));
    }
    //@formatter:on

    // stores a binding for each term
    private final SymbolTable<Binding, Void> terms = new SymbolTable<>(STD_TERMS, null);

    // stores the "resolved" type of each type
    private final SymbolTable<RuntimeType, Void> types = new SymbolTable<>(STD_TYPES, null);
    private final List<SemanticException>  errors = new ArrayList<>();

    public List<SemanticException> check(final Statement program) {
        visit(program);
        return errors;
    }

    // the methods are named visit() to help readers familiar with the visitor-pattern understand this code

    private void visit(final Argument argument) throws SemanticException {
        switch (argument) {
            case Argument.FuncArgument funcArgument -> {
                Identifier func = funcArgument.func();

                visit(func);
                if (!(func.getType() instanceof FuncType funcType)) {
                    throw new SemanticException.TypeError(funcArgument.sourcePos(), func.getType(), "function");
                }

                funcArgument.setType(funcType);
            }
            case Argument.VarArgument varArgument -> {
                Identifier var = varArgument.var();

                visit(var);
                if (var.getType() instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(varArgument.sourcePos(), var.getType(), "not a function");
                }

                varArgument.setType(var.getType());
            }
            case Expression expression -> {
                visit(expression);
                if (expression.getType() instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC/PROC
                    throw new SemanticException.TypeError(expression.sourcePos(), expression.getType(), "not a function");
                }
            }
        }
    }

    // analyze(Declaration) needs to throw SemanticException instead of merely adding errors to the list, because it does not
    // know to what point to "rewind" to to continue analysis
    private void visit(final Declaration declaration) throws SemanticException {
        switch (declaration) {
            case ConstDeclaration constDeclaration -> {
                try {
                    visit(constDeclaration.value());
                    terms.add(constDeclaration.name(), new Binding(constDeclaration.value().getType(), true, constDeclaration));
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(constDeclaration.sourcePos(), e.getFieldType());
                }
            }
            case Declaration.FuncDeclaration funcDeclaration -> {
                // check for duplicate parameter
                Set<String> seenParameters = new HashSet<>();
                List<RuntimeType> resolvedParamTypes = new ArrayList<>();

                for (Parameter param : funcDeclaration.parameters()) {
                    if (seenParameters.contains(param.getName())) {
                        throw new SemanticException.DuplicateParameter(funcDeclaration.sourcePos(), param);
                    }

                    seenParameters.add(param.getName());
                    // resolve the type of the parameter in the current env
                    visit(param);
                    resolvedParamTypes.add(param.getType());
                }

                RuntimeType funcType;
                // inside the function body
                types.enterNewScope(null);
                terms.enterNewScope(null);

                try {
                    // assign each parameter to a basic identifier with its resolved type
                    for (int i = 0; i < funcDeclaration.parameters().size(); i++) {
                        Parameter p = funcDeclaration.parameters().get(i);
                        // parameters dont have a declaration
                        terms.add(p.getName(), new Binding(p.getType(), false, null));
                    }

                    RuntimeType resolvedReturnType = resolveType(funcDeclaration.declaredReturnType());

                    // (optimistically) assign the function its declared return type
                    funcType = new FuncType(resolvedParamTypes, resolvedReturnType);
                    // func is constant in the body of the function,
                    terms.add(funcDeclaration.name(), new Binding(funcType, true, funcDeclaration));

                    // then type check the function body
                    visit(funcDeclaration.expression());

                    SourcePosition sourcePos = funcDeclaration.sourcePos();

                    // if final inferred type is different from declared return type, error
                    if (!funcDeclaration.expression().getType().equals(resolvedReturnType)) {
                        throw new SemanticException.TypeError(
                                sourcePos, resolvedReturnType, funcDeclaration.expression().getType());
                    }
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(funcDeclaration.sourcePos(), e.getFieldType());
                } finally {
                    // remember to exit the newly created scope even if analysis fails
                    types.exitScope();
                    terms.exitScope();
                }

                // add the newly declared function to this scopes terms
                // functions are always constant since we dont support HOF
                terms.add(funcDeclaration.name(), new Binding(funcType, true, funcDeclaration));
            }
            case TypeDeclaration typeDeclaration -> {
                try {
                    // resolve the type and add it to the symbol table
                    RuntimeType resolvedType = resolveType(typeDeclaration.type());
                    types.add(typeDeclaration.name(), resolvedType);
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(typeDeclaration.sourcePos(), e.getFieldType());
                }
            }
            case Declaration.VarDeclaration varDeclaration -> {
                try {
                    // resolve the type, set the declarations new type to the resolved type, and add a binding to the symbol table
                    RuntimeType rType = resolveType(varDeclaration.declaredType());
                    varDeclaration.setRuntimeType(rType);
                    terms.add(varDeclaration.name(), new Binding(rType, false, varDeclaration));
                } catch (SemanticException.DuplicateRecordTypeField e) {
                    // rethrow duplicate record fields with added source position info
                    throw new SemanticException.DuplicateRecordTypeField(varDeclaration.sourcePos(), e.getFieldType());
                }
            }
            // this is very similar to the funcDeclaration case, just no return type
            case Declaration.ProcDeclaration procDeclaration -> {
                // check for duplicate parameter
                Set<String> seenParameters = new HashSet<>();
                List<RuntimeType> resolvedParamTypes = new ArrayList<>();

                for (Parameter param : procDeclaration.parameters()) {
                    if (seenParameters.contains(param.getName())) {
                        throw new SemanticException.DuplicateParameter(procDeclaration.sourcePos(), param);
                    }

                    // resolve the type of the parameter in the current env
                    visit(param);
                    seenParameters.add(param.getName());
                    resolvedParamTypes.add(param.getType());
                }

                RuntimeType funcType;
                // inside the function body
                types.enterNewScope(null);
                terms.enterNewScope(null);
                try {
                    // assign each parameter to a basic identifier with its resolved type
                    for (int i = 0; i < procDeclaration.parameters().size(); i++) {
                        Parameter p = procDeclaration.parameters().get(i);
                        // parameters dont have a declaration
                        terms.add(p.getName(), new Binding(p.getType(), false, null));
                    }

                    // (optimistically) assign the function its declared return type
                    funcType = new FuncType(resolvedParamTypes, VOID_TYPE);
                    // func is constant in the body of the function,
                    terms.add(procDeclaration.name(), new Binding(funcType, true, procDeclaration));

                    // then type check the function body
                    visit(procDeclaration.statement());
                } finally {
                    // remember to exit the newly created scope even if analysis fails
                    types.exitScope();
                    terms.exitScope();
                }

                // add the newly declared function to this scopes terms
                // functions are always constant since we dont support HOF
                terms.add(procDeclaration.name(), new Binding(funcType, true, procDeclaration));
            }
        }
    }

    private void visit(final Identifier identifier) throws SemanticException {
        switch (identifier) {
            case Identifier.ArraySubscript arraySubscript -> {
                Identifier array = arraySubscript.array();
                Expression subscript = arraySubscript.subscript();

                visit(array);
                if (!(array.getType() instanceof ArrayType arrayType)) {
                    throw new SemanticException.TypeError(arraySubscript.sourcePos(), array.getType(), "array");
                }

                visit(subscript);
                if (!(subscript.getType() instanceof PrimType.IntType)) {
                    throw new SemanticException.TypeError(arraySubscript.sourcePos(), subscript.getType(), INT_TYPE);
                }

                arraySubscript.setType(arrayType.elementType());
            }
            case BasicIdentifier basicIdentifier -> {
                Binding binding = lookup(basicIdentifier);
                basicIdentifier.setType(binding.type());
                basicIdentifier.setDeclaration(binding.declaration());
            }
            case Identifier.RecordAccess recordAccess -> {
                Identifier record = recordAccess.record();
                Identifier field = recordAccess.field();

                visit(record);
                if (!(record.getType() instanceof RecordType recordType)) {
                    throw new SemanticException.TypeError(recordAccess.sourcePos(), record.getType(), "record");
                }

                // record access has a new scope with the field names and types of the record available
                types.enterNewScope(null);
                for (RecordType.FieldType fieldType : recordType.fieldTypes()) {
                    // record fields dont have a declaration
                    terms.add(fieldType.fieldName(), new Binding(fieldType.fieldType(), true, null));
                }
                visit(field);
                types.exitScope();

                // TODO:
                recordAccess.setType(field.getType());
            }
        }
    }

    private Binding lookup(final BasicIdentifier basicIdentifier) throws SemanticException {
        try {
            return terms.lookup(basicIdentifier.name());
        } catch (NoSuchElementException e) {
            throw new SemanticException.UndeclaredUse(basicIdentifier.sourcePos(), basicIdentifier);
        }
    }

    // analyze(Expression) needs to throw SemanticException; since an expression may be part of a larger declaration and it
    // wont know where to rewind to
    private void visit(final Expression expression) throws SemanticException {
        switch (expression) {
            case BinaryOp binop -> {
                BasicIdentifier operator = binop.operator();
                Expression leftOperand = binop.leftOperand();
                Expression rightOperand = binop.rightOperand();

                // throw exception early if we cant find the operator
                Binding opBinding = lookup(operator);
                visit(leftOperand);
                visit(rightOperand);

                // I special-case the equality operations because they are the ONLY polymorphic function and I do not have enough
                //  time to implement full polymorphism
                if (operator.name().equals("=")) {
                    // just make sure that left and right operands are the same type
                    if (!leftOperand.getType().equals(rightOperand.getType())) {
                        throw new SemanticException.TypeError(binop.sourcePos(), leftOperand.getType(), rightOperand.getType());
                    }

                    binop.setType(BOOL_TYPE);
                    return;
                }
                if (operator.name().equals("\\=")) {
                    // just make sure that left and right operands are the same type
                    if (!leftOperand.getType().equals(rightOperand.getType())) {
                        throw new SemanticException.TypeError(binop.sourcePos(), leftOperand.getType(), rightOperand.getType());
                    }

                    binop.setType(BOOL_TYPE);
                    return;
                }
                // make sure the above special-case block precedes any attempt to access opType's value, since the equality
                //  operations have their types set to null in the stdenv

                if (!(opBinding.type() instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(binop.sourcePos(), opBinding.type(), "function");
                }

                if (argTypes.size() != 2) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(binop.sourcePos(), operator, 2, argTypes.size());
                }

                if (!leftOperand.getType().equals(argTypes.get(0))) {
                    throw new SemanticException.TypeError(binop.sourcePos(), argTypes.get(0), leftOperand.getType());
                }

                if (!rightOperand.getType().equals(argTypes.get(1))) {
                    throw new SemanticException.TypeError(binop.sourcePos(), argTypes.get(1), rightOperand.getType());
                }

                binop.setType(returnType);
            }
            case FunCall funCall -> {
                Identifier callable = funCall.func();
                List<Argument> arguments = funCall.arguments();

                visit(callable);

                if (!(callable.getType() instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(funCall.sourcePos(), callable.getType(), "function");
                }

                if (argTypes.size() != arguments.size()) {
                    throw new SemanticException.ArityMismatch(funCall.sourcePos(), callable, argTypes.size(), arguments.size());
                }

                // for each argument
                Declaration declaration = lookup(funCall.func()).declaration();
                if (declaration instanceof Declaration.ProcDeclaration procDeclaration) {
                    checkArgumentTypes(arguments, procDeclaration.parameters());
                } else if (declaration instanceof Declaration.FuncDeclaration funcDeclaration) {
                    checkArgumentTypes(arguments, funcDeclaration.parameters());
                }

                for (int i = 0; i < argTypes.size(); i++) {
                    Argument arg = arguments.get(i);

                    // we dont have to resolveType() the types in function arg list since it should have been done at
                    // declaration time
                    RuntimeType expectedType = argTypes.get(i);

                    // analyze it
                    visit(arg);

                    if (!(arg.getType().equals(expectedType))) {
                        throw new SemanticException.TypeError(funCall.sourcePos(), arg.getType(), expectedType);
                    }
                }

                funCall.setType(returnType);
            }
            case Identifier identifier -> {
                visit(identifier);
                // cannot evaluate a function as a result since we dont support HOF
                if (identifier.getType() instanceof FuncType) {
                    throw new SemanticException.FunctionResult(identifier.sourcePos(), identifier);
                }
            }
            case IfExpression ifExpression -> {
                Expression condition = ifExpression.condition();
                Expression consequent = ifExpression.consequent();
                Expression alternative = ifExpression.alternative();

                visit(condition);
                if (!(condition.getType() instanceof PrimType.BoolType)) {
                    throw new SemanticException.TypeError(ifExpression.sourcePos(), condition.getType(), BOOL_TYPE);
                }

                visit(consequent);
                visit(alternative);
                if (!(consequent.getType().equals(alternative.getType()))) {
                    throw new SemanticException.TypeError(ifExpression.sourcePos(), consequent.getType(), alternative.getType());
                }

                ifExpression.setType(consequent.getType());
            }
            case LetExpression letExpression -> {
                List<Declaration> declarations = letExpression.declarations();
                Expression expr = letExpression.expression();

                types.enterNewScope(null);

                // the new scope has all the declared identifiers bound to their types
                for (Declaration declaration : declarations) {
                    visit(declaration);
                }
                // the expression is evaluated in the new environment
                visit(expr);

                types.exitScope();

                letExpression.setType(expr.getType());
            }
            case LitArray litArray -> {
                SourcePosition sourcePos = litArray.sourcePos();
                List<Expression> values = litArray.elements();

                // type of empty array?
                if (values.isEmpty()) {
                    throw new UnsupportedOperationException();
                }

                visit(values.getFirst());
                RuntimeType expectedType = values.getFirst().getType();
                for (Expression value : values) {
                    visit(value);
                    if (!value.getType().equals(expectedType)) {
                        throw new SemanticException.TypeError(sourcePos, expectedType, value.getType());
                    }
                }

                litArray.setType(new ArrayType(values.size(), expectedType));
            }
            case LitChar _ -> { }
            case LitInt _ -> { }
            case LitRecord litRecord -> {

                SourcePosition sourcePos = litRecord.sourcePos();
                List<LitRecord.RecordField> fields = litRecord.fields();

                if (fields.isEmpty()) {
                    // implementing this sensibly will take row-poly types which is too hard so we just treat all empty records
                    // as belonging to a single special empty-record type
                    litRecord.setType(new RecordType(Collections.emptyList()));
                    return;
                }

                List<RecordType.FieldType> fieldTypes = new ArrayList<>(fields.size());
                Set<String> seenFieldNames = new HashSet<>();
                for (LitRecord.RecordField field : fields) {
                    if (seenFieldNames.contains(field.name())) {
                        throw new SemanticException.DuplicateRecordField(sourcePos, field);
                    }
                    seenFieldNames.add(field.name());
                    visit(field.value());
                    fieldTypes.add(new RecordType.FieldType(field.name(), field.value().getType()));
                }

                litRecord.setType(new RecordType(fieldTypes));
            }
            case UnaryOp unaryOp -> {
                BasicIdentifier operator = unaryOp.operator();
                Expression operand = unaryOp.operand();

                // throw exception early if we cant find the operator
                Binding opBinding = lookup(operator);
                if (!(opBinding.type() instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(unaryOp.sourcePos(), opBinding.type(), "function");
                }

                if (argTypes.size() != 1) {
                    // since we dont allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(unaryOp.sourcePos(), operator, 1, argTypes.size());
                }

                visit(operand);
                RuntimeType expectedType = argTypes.getFirst();
                if (!operand.getType().equals(expectedType)) {
                    throw new SemanticException.TypeError(unaryOp.sourcePos(), expectedType, operand.getType());
                }

                unaryOp.setType(returnType);
            }
            case LitBool _ -> { }
        }
    }

    // checks if an argument list
    private static void checkArgumentTypes(final List<Argument> arguments, final List<Parameter> declaredParams)
            throws SemanticException {
        assert arguments.size() == declaredParams.size();
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            Parameter param = declaredParams.get(i);

            // if the corresponding parameter was declared var, the argument must be too
            if (param instanceof Parameter.VarParameter && !(arg instanceof Argument.VarArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Parameter.VarParameter.class);
            }

            // if the corresponding parameter was declared func, the argument must be too
            if (param instanceof Parameter.FuncParameter && !(arg instanceof Argument.FuncArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Parameter.FuncParameter.class);
            }

            if (param instanceof Parameter.ValueParameter && !(arg instanceof Expression)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Parameter.ValueParameter.class);
            }
        }
    }

    private void visit(final Parameter parameter) throws SemanticException {
        switch (parameter) {
            case Parameter.FuncParameter funcParameter -> {
                List<Parameter> parameters = funcParameter.parameters();
                Type returnType = funcParameter.declaredReturnType();

                List<RuntimeType> paramTypes = new ArrayList<>();
                for (Parameter p : parameters) {
                    visit(p);
                    paramTypes.add(p.getType());
                }

                parameter.setType(new FuncType(paramTypes, resolveType(returnType)));
            }

            // resolve parameter type in current environment and set params type to resolved version
            case Parameter.VarParameter varParameter -> parameter.setType(resolveType(varParameter.declaredType()));
            case Parameter.ValueParameter valueParameter -> parameter.setType(resolveType(valueParameter.declaredType()));
        }
    }

    private void visit(final Statement statement) {
        switch (statement) {
            case Statement.ExpressionStatement expressionStatement -> {
                try {
                    visit(expressionStatement.expression());
                } catch (SemanticException e) {
                    errors.add(e);
                }
            }
            case Statement.AssignStatement assignStatement -> {
                Identifier lvalue = assignStatement.identifier();
                Expression rvalue = assignStatement.expression();

                try {
                    // TODO: does this even work rn?
                    // bit of a hack-ish way of ensure the user doesnt circumvent const-protection by passing in, say a var to
                    //  the const
                    if (terms.lookupAll(lvalue.root().name()).stream().anyMatch(Binding::constant)) {
                        errors.add(new SemanticException.AssignmentToConstant(assignStatement.sourcePos(), lvalue));
                    }

                    visit(rvalue);
                    visit(lvalue);
                    if (!lvalue.getType().equals(rvalue.getType())) {
                        errors.add(
                                new SemanticException.TypeError(assignStatement.sourcePos(), lvalue.getType(), rvalue.getType()));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }
            }
            case Statement.IfStatement ifStatement -> {
                Expression condition = ifStatement.condition();
                Optional<Statement> consequent = ifStatement.consequent();
                Optional<Statement> alternative = ifStatement.alternative();

                try {
                    visit(condition);
                    if (!(condition.getType() instanceof PrimType.BoolType)) {
                        errors.add(new SemanticException.TypeError(ifStatement.sourcePos(), condition.getType(), BOOL_TYPE));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }

                consequent.ifPresent(this::visit);
                alternative.ifPresent(this::visit);
            }
            case Statement.LetStatement letStatement -> {
                List<Declaration> declarations = letStatement.declarations();
                Statement stmt = letStatement.statement();

                terms.enterNewScope(null);
                types.enterNewScope(null);

                // declared identifiers get bound in symtab in analyze(Declaration)
                for (Declaration declaration : declarations) {
                    try {
                        visit(declaration);
                    } catch (SemanticException e) {
                        errors.add(e);
                    }
                }

                // analyze the statement in the new environment
                visit(stmt);
                terms.exitScope();
                types.exitScope();
            }
            case Statement.LoopWhileStatement loopWhileStatement -> {
                Expression condition = loopWhileStatement.condition();
                Statement loopBody = loopWhileStatement.loopBody();
                Statement doBody = loopWhileStatement.doBody();

                try {
                    visit(condition);
                    if (!(condition.getType() instanceof PrimType.BoolType)) {
                        errors.add(
                                new SemanticException.TypeError(loopWhileStatement.sourcePos(), condition.getType(), BOOL_TYPE));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }

                visit(loopBody);
                visit(doBody);
            }
            case Statement.RepeatUntilStatement repeatUntilStatement -> {
                Expression condition = repeatUntilStatement.condition();
                Statement body = repeatUntilStatement.body();

                try {
                    visit(condition);
                    if (!(condition.getType() instanceof PrimType.BoolType)) {
                        errors.add(new SemanticException.TypeError(repeatUntilStatement.sourcePos(), condition.getType(),
                                                                   BOOL_TYPE
                        ));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }

                visit(body);
            }
            case Statement.RepeatWhileStatement repeatWhileStatement -> {
                Expression condition = repeatWhileStatement.condition();
                Statement body = repeatWhileStatement.body();

                try {
                    visit(condition);
                    if (!(condition.getType() instanceof PrimType.BoolType)) {
                        errors.add(new SemanticException.TypeError(repeatWhileStatement.sourcePos(), condition.getType(),
                                                                   BOOL_TYPE
                        ));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }

                visit(body);
            }
            case Statement.StatementBlock statementBlock -> {
                for (Statement stmt : statementBlock.statements()) {
                    visit(stmt);
                }
            }
            case Statement.WhileStatement whileStatement -> {
                Expression condition = whileStatement.condition();
                Statement body = whileStatement.body();

                try {
                    visit(condition);
                    if (!(condition.getType() instanceof PrimType.BoolType)) {
                        errors.add(new SemanticException.TypeError(whileStatement.sourcePos(), condition.getType(), BOOL_TYPE));
                    }
                } catch (SemanticException e) {
                    errors.add(e);
                }

                visit(body);
            }
        }
    }

    // all syntactic types, i.e., syntactic phrases used as types in the source code must resolve to an "actual" type -
    //  RuntimeType
    private RuntimeType resolveType(final Type type) throws SemanticException {
        return switch (type) {
            case Type.ArrayType(int size, Type elementType) -> new ArrayType(size, resolveType(elementType));
            case Type.RecordType recordType -> {
                Set<String> seenFieldNames = new HashSet<>();
                List<RecordType.FieldType> resolvedFieldTypes = new ArrayList<>();
                for (Type.RecordType.FieldType field : recordType.fieldTypes()) {
                    // check for duplicate fields in the record type definition
                    if (seenFieldNames.contains(field.fieldName())) {
                        throw new SemanticException.DuplicateRecordTypeField(field);
                    }

                    RecordType.FieldType resolvedField = new RecordType.FieldType(
                            field.fieldName(), resolveType(field.fieldType()));
                    resolvedFieldTypes.add(resolvedField);
                    seenFieldNames.add(field.fieldName());
                }

                yield new RecordType(resolvedFieldTypes);
            }
            case BasicType basicType -> lookup(basicType);
            case Type.Void _ -> VOID_TYPE;
        };
    }

    private RuntimeType lookup(final BasicType basicType) throws SemanticException {
        try {
            return types.lookup(basicType.name());
        } catch (NoSuchElementException e) {
            throw new SemanticException.UndeclaredUse(basicType);
        }
    }

    // each identifier is bound to its type, whether or not its constant, and the place where it was declared
    record Binding(RuntimeType type, boolean constant, Declaration declaration) { }

}
