package triangle.analysis;

import triangle.abstractMachine.Machine;
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
import triangle.ast.Expression.LitBool;
import triangle.ast.Expression.LitChar;
import triangle.ast.Expression.LitInt;
import triangle.ast.Expression.LitRecord;
import triangle.ast.Expression.UnaryOp;
import triangle.ast.Parameter;
import triangle.ast.SourcePosition;
import triangle.ast.Statement;
import triangle.ast.Type;
import triangle.ast.RuntimeType;
import triangle.ast.RuntimeType.PrimType.FuncType;
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
import static triangle.ast.RuntimeType.*;

// semantic analysis does several things:
//      it checks that all uses of an identifier are in a scope with a corresponding declaration
//      it updates the AST to annotated all uses of an identifier with its corresponding declaration
//      it checks that all expressions have the correct type for how they are being used
//      it updates the AST to annotated all Typeable nodes with their types; i.e., it produces an explicitly-typed AST
//      it ensures that the program does not attempt to return a function/procedure as a value, as HOF are not supported

// WARNING: this class uses exception as control flow; this is to allow checking to resume from a known "safe point"
public final class SemanticAnalyzer {

    //@formatter:off
    private static final RuntimeType              binaryRelation    =
            new FuncType(List.of(BOOL_TYPE, BOOL_TYPE), BOOL_TYPE);
    private static final RuntimeType              binaryIntRelation =
            new FuncType(List.of(INT_TYPE, INT_TYPE), BOOL_TYPE);
    private static final RuntimeType              binaryIntFunc     =
            new FuncType(List.of(INT_TYPE, INT_TYPE), INT_TYPE);
    private static final Map<String, Binding>     STD_TERMS         =
            new HashMap<>();
    private static final Map<String, RuntimeType> STD_TYPES         =
            Map.of(
                    "Integer", INT_TYPE,
                    "Char", CHAR_TYPE,
                    "Boolean",BOOL_TYPE
            );

    // stdenv values have null as annotation
    static {
        // TODO: this is very hackish and ugly
        STD_TERMS.putAll(
                Map.of(
                        "\\/", new Binding(binaryRelation),
                        "/\\", new Binding(binaryRelation),
                        "<=",  new Binding(binaryIntRelation),
                        ">=",  new Binding(binaryIntRelation),
                        ">",   new Binding(binaryIntRelation),
                        "<",   new Binding(binaryIntRelation),
                        "\\",  new Binding(new FuncType(List.of(BOOL_TYPE), BOOL_TYPE))
                ));

        STD_TERMS.putAll(
                Map.of(
                        "-",  new Binding(binaryIntFunc),
                        "+",  new Binding(binaryIntFunc),
                        "*",  new Binding(binaryIntFunc),
                        "/",  new Binding(binaryIntFunc),
                        "//", new Binding(binaryIntFunc),
                        "|",  new Binding(new FuncType(List.of(INT_TYPE), INT_TYPE)),
                        "++", new Binding(new FuncType(List.of(INT_TYPE), INT_TYPE))
        ));

        // these are set to void just as dummy values so that we fail fast in case something tries to access their types since
        //  these are supposed to be special-cased in analyze(Expression)
        STD_TERMS.putAll(
                Map.of(
                        "=", new Binding(VOID_TYPE),
                        "\\=", new Binding(VOID_TYPE)
                ));

        STD_TERMS.putAll(
                Map.of(
                        "get",      new Binding(new FuncType(List.of(new RefOf(CHAR_TYPE)), VOID_TYPE)),
                        "getint",   new Binding(new FuncType(List.of(new RefOf(INT_TYPE)), VOID_TYPE)),
                        "geteol",   new Binding(new FuncType(List.of(), VOID_TYPE)),
                        "puteol",   new Binding(new FuncType(List.of(), VOID_TYPE)),
                        "put",      new Binding(new FuncType(List.of(CHAR_TYPE), VOID_TYPE)),
                        "putint",   new Binding(new FuncType(List.of(INT_TYPE), VOID_TYPE)),
                        "chr",      new Binding(new FuncType(List.of(INT_TYPE), CHAR_TYPE)),
                        "eol",      new Binding(new FuncType(List.of(), BOOL_TYPE)),
                        "ord",      new Binding(new FuncType(List.of(CHAR_TYPE), INT_TYPE))
                ));
    }
    //@formatter:on

    private static void checkArgumentTypes(final List<Argument> arguments, final List<Parameter> declaredParams)
            throws SemanticException {
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            Parameter param = declaredParams.get(i);

            // if the corresponding parameter was declared var, the argument must be too
            if (param instanceof Parameter.VarParameter && !(arg instanceof Argument.VarArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Argument.VarArgument.class);
            }

            // if the corresponding parameter was declared func, the argument must be too
            if (param instanceof Parameter.FuncParameter && !(arg instanceof Argument.FuncArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Argument.FuncArgument.class);
            }

            if (param instanceof Parameter.ValueParameter && !(arg instanceof Expression)) {
                throw new SemanticException.InvalidArgument(arg.sourcePos(), arg, Expression.class);
            }
        }
    }

    private final SymbolTable<Binding, Void>     terms  = new SymbolTable<>(STD_TERMS, null);
    private final SymbolTable<RuntimeType, Void> types  = new SymbolTable<>(STD_TYPES, null);
    private final List<SemanticException>        errors = new ArrayList<>();
    private final Statement                      program;

    public SemanticAnalyzer(final Statement program) {
        this.program = program;
    }

    public List<SemanticException> getErrors() {
        return errors;
    }

    public void check() {
        visit(program);
    }

    private void visit(final Argument argument) throws SemanticException {
        switch (argument) {
            case Argument.FuncArgument funcArgument -> {
                Identifier func = funcArgument.func();

                visit(func);
                RuntimeType funcType = func.getType().baseType();
                if (!(funcType instanceof FuncType)) {
                    throw new SemanticException.TypeError(funcArgument.sourcePos(), funcType, "function");
                }

                funcArgument.setType(funcType);
            }
            case Argument.VarArgument varArgument -> {
                Identifier var = varArgument.var();

                visit(var);
                RuntimeType varType = var.getType().baseType();
                if (varType instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC
                    throw new SemanticException.TypeError(varArgument.sourcePos(), varType, "not a function");
                }

                varArgument.setType(var.getType());
            }
            case Expression expression -> {
                visit(expression);
                RuntimeType exprType = expression.getType().baseType();
                if (exprType instanceof FuncType) {
                    // arguments are not allowed to be function types if they are not declared FUNC/PROC
                    throw new SemanticException.TypeError(expression.sourcePos(), exprType, "not a function");
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
                        // parameters don't have a declaration
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
                // functions are always constant since we don't support HOF
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
                        // parameters don't have a declaration
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
                // functions are always constant since we don't support HOF
                terms.add(procDeclaration.name(), new Binding(funcType, true, procDeclaration));
            }
        }
    }

    private void visit(final Identifier identifier) throws SemanticException {
        switch (identifier) {
            case Identifier.ArraySubscript arraySubscript -> {
                Identifier array = arraySubscript.array();
                Expression subscript = arraySubscript.subscript();

                // "unpack" the types, if they are refs, since we want to treat var arguments transparently

                // this means that:
                //   arr[0] has type arr.elementType if arr is not a ref
                //              type RefOf(arr.elementType) if arr is a RefOf(refType)

                visit(array);
                RuntimeType rArrayType = array.getType().baseType();
                if (!(rArrayType instanceof ArrayType arrayType)) {
                    throw new SemanticException.TypeError(arraySubscript.sourcePos(), rArrayType, "array");
                }

                visit(subscript);
                RuntimeType subscriptType = subscript.getType().baseType();
                if (!(subscriptType instanceof PrimType.IntType)) {
                    throw new SemanticException.TypeError(arraySubscript.sourcePos(), subscriptType, INT_TYPE);
                }

                RuntimeType elementType = arrayType.elementType();
                // repack the type if needed
                arraySubscript.setType(array.getType() instanceof RefOf ? new RefOf(elementType) : elementType);
            }
            case BasicIdentifier basicIdentifier -> {
                Binding binding = lookup(basicIdentifier);
                // if the identifier is expected to be a reference, then assign it a RefOf() type
                basicIdentifier.setType(binding.type());
            }
            case Identifier.RecordAccess recordAccess -> {
                Identifier record = recordAccess.record();
                Identifier field = recordAccess.field();

                // same as ArraySubscript, we want to treat record field access transparently wrt references

                visit(record);
                RuntimeType rRecordType = record.getType().baseType();
                if (!(rRecordType instanceof RecordType recordType)) {
                    throw new SemanticException.TypeError(recordAccess.sourcePos(), rRecordType, "record");
                }

                // record access has a new type-scope with the field names and types of the record available
                types.enterNewScope(null);
                for (RecordType.FieldType fieldType : recordType.fieldTypes()) {
                    // the record field is a reference iff the main record (that is being accessed) is
                    // record fields don't have a declaration
                    // repack the field types if needed
                    RuntimeType rFieldType =
                            record.getType() instanceof RefOf ?
                                    new RefOf(fieldType.fieldType()) :
                                    fieldType.fieldType();
                    terms.add(fieldType.fieldName(), new Binding(rFieldType, true, null));
                }
                visit(field);
                types.exitScope();

                // field.getType() will be a RefOf iff record.getType() is RefOf
                // this is ensured by case BasicIdentifier and the above loop
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

                Binding opBinding = lookup(operator);

                visit(leftOperand);
                visit(rightOperand);

                RuntimeType lOperandType = leftOperand.getType().baseType();
                RuntimeType rOperandType = rightOperand.getType().baseType();

                // I special-case the equality operations because they are the ONLY polymorphic functions and I do not have enough
                //  time to implement full polymorphism
                if (operator.name().equals("=") || operator.name().equals("\\=")) {
                    // just make sure that left and right operands are the same type
                    if (!lOperandType.equals(rOperandType)) {
                        throw new SemanticException.TypeError(binop.sourcePos(), lOperandType, rOperandType);
                    }

                    binop.setType(BOOL_TYPE);
                    return;
                }
                // just make sure that left and right operands are the same type
                // make sure the above special-case block precedes any attempt to access opType's value, since the equality
                //  operations have their types set to null in the stdenv

                RuntimeType operatorType = opBinding.type().baseType();
                if (!(operatorType instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(binop.sourcePos(), operatorType, "function");
                }

                if (argTypes.size() != 2) {
                    // since we don't allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(binop.sourcePos(), operator, 2, argTypes.size());
                }

                if (!lOperandType.equals(argTypes.get(0))) {
                    //noinspection SequencedCollectionMethodCanBeUsed
                    throw new SemanticException.TypeError(binop.sourcePos(), argTypes.get(0), lOperandType);
                }

                if (!rOperandType.equals(argTypes.get(1))) {
                    throw new SemanticException.TypeError(binop.sourcePos(), argTypes.get(1), rOperandType);
                }

                binop.setType(returnType);
            }
            case FunCall funCall -> {
                Identifier callable = funCall.func();
                List<Argument> arguments = funCall.arguments();

                visit(callable);

                RuntimeType funcType = callable.getType().baseType();

                if (!(funcType instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(funCall.sourcePos(), funcType, "function");
                }

                if (argTypes.size() != arguments.size()) {
                    throw new SemanticException.ArityMismatch(funCall.sourcePos(), callable, argTypes.size(), arguments.size());
                }

                Declaration declaration = lookup(funCall.func()).declaration();
                // if there is a corresponding declaration for this function (i.e., it is not a primitive call) check if the
                //  arguments being passed correspond to the parameters; TAM specification requires that a parameter declared var
                //  must have a corresponding argument declared var; etc.
                if (declaration instanceof Declaration.ProcDeclaration procDeclaration) {
                    checkArgumentTypes(arguments, procDeclaration.parameters());
                } else if (declaration instanceof Declaration.FuncDeclaration funcDeclaration) {
                    checkArgumentTypes(arguments, funcDeclaration.parameters());
                }

                for (int i = 0; i < argTypes.size(); i++) {
                    Argument arg = arguments.get(i);

                    // we don't have to resolve the types in argTypes since it should have been done at declaration time
                    RuntimeType expectedType = argTypes.get(i).baseType();

                    // analyze it
                    visit(arg);
                    RuntimeType argType = arg.getType().baseType();
                    if (!(argType.equals(expectedType))) {
                        throw new SemanticException.TypeError(funCall.sourcePos(), argType, expectedType);
                    }
                }

                funCall.setType(returnType);
            }
            case Identifier identifier -> {
                visit(identifier);
                // cannot evaluate a function as a result since we don't support HOF
                final RuntimeType identifierType = identifier.getType().baseType();
                if (identifierType instanceof FuncType) {
                    throw new SemanticException.FunctionResult(identifier.sourcePos(), identifier);
                }
            }
            case IfExpression ifExpression -> {
                Expression condition = ifExpression.condition();
                Expression consequent = ifExpression.consequent();
                Expression alternative = ifExpression.alternative();

                visit(condition);
                RuntimeType condType = condition.getType();
                if (!(condType instanceof PrimType.BoolType)) {
                    throw new SemanticException.TypeError(ifExpression.sourcePos(), condType, BOOL_TYPE);
                }

                visit(consequent);
                visit(alternative);
                RuntimeType conseqType = consequent.getType();
                RuntimeType alternType = alternative.getType();
                if (!(conseqType.equals(alternType))) {
                    throw new SemanticException.TypeError(ifExpression.sourcePos(), conseqType, alternType);
                }

                ifExpression.setType(conseqType);
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

                letExpression.setType(expr.getType().baseType());
            }
            case LitArray litArray -> {
                SourcePosition sourcePos = litArray.sourcePos();
                List<Expression> values = litArray.elements();

                // type of empty array?
                if (values.isEmpty()) {
                    throw new UnsupportedOperationException();
                }

                visit(values.getFirst());
                RuntimeType expectedType = values.getFirst().getType().baseType();
                for (Expression value : values) {
                    visit(value);
                    RuntimeType valueType = value.getType().baseType();
                    if (!valueType.equals(expectedType)) {
                        throw new SemanticException.TypeError(sourcePos, expectedType, valueType);
                    }
                }

                litArray.setType(new ArrayType(values.size(), expectedType));
            }
            case LitChar _ -> { }
            case LitInt(SourcePosition sourcePos, int value) -> {
                // the TAM specification defines `Integer` to be between -maxint...+maxint (not two's complement!)
                if (value > Machine.maxintRep || value < (Machine.maxintRep * -1)) {
                    throw new SemanticException.IntegerLiteralTooLarge(sourcePos, value);
                }
            }
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
                RuntimeType opType = opBinding.type().baseType();
                if (!(opType instanceof FuncType(List<RuntimeType> argTypes, RuntimeType returnType))) {
                    throw new SemanticException.TypeError(unaryOp.sourcePos(), opType, "function");
                }

                if (argTypes.size() != 1) {
                    // since we don't allow custom operator definitions, this can only happen if we mess up the stdenv
                    throw new SemanticException.ArityMismatch(unaryOp.sourcePos(), operator, 1, argTypes.size());
                }

                visit(operand);
                RuntimeType expectedType = argTypes.getFirst().baseType();
                RuntimeType operandType = operand.getType().baseType();
                if (!operandType.equals(expectedType)) {
                    throw new SemanticException.TypeError(unaryOp.sourcePos(), expectedType, operandType);
                }

                unaryOp.setType(returnType);
            }
            case LitBool _ -> { }
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
            case Parameter.VarParameter varParameter -> parameter.setType(new RefOf(resolveType(varParameter.declaredType())));
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
                    // bit of a hack-ish way of ensure the user doesn't circumvent const-protection by passing in, say a var to
                    //  the const
                    if (terms.lookupAll(lvalue.root().name()).stream().anyMatch(Binding::constant)) {
                        errors.add(new SemanticException.AssignmentToConstant(assignStatement.sourcePos(), lvalue));
                    }

                    visit(rvalue);
                    visit(lvalue);
                    RuntimeType lType = lvalue.getType().baseType();
                    RuntimeType rType = rvalue.getType().baseType();
                    if (!lType.equals(rType)) {
                        errors.add(new SemanticException.TypeError(assignStatement.sourcePos(), lType, rType));
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
                    RuntimeType condType = condition.getType().baseType();
                    if (!(condType instanceof PrimType.BoolType)) {
                        errors.add(new SemanticException.TypeError(ifStatement.sourcePos(), condType, BOOL_TYPE));
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
                visitLoop(loopWhileStatement.sourcePos(), loopWhileStatement.condition(), loopWhileStatement.loopBody());
                visit(loopWhileStatement.doBody());
            }
            case Statement.RepeatUntilStatement repeatUntilStatement -> visitLoop(
                    repeatUntilStatement.sourcePos(), repeatUntilStatement.condition(), repeatUntilStatement.body());
            case Statement.RepeatWhileStatement repeatWhileStatement -> visitLoop(
                    repeatWhileStatement.sourcePos(), repeatWhileStatement.condition(), repeatWhileStatement.body());
            case Statement.StatementBlock statementBlock -> {
                for (Statement stmt : statementBlock.statements()) {
                    visit(stmt);
                }
            }
            case Statement.WhileStatement whileStatement -> visitLoop(
                    whileStatement.sourcePos(), whileStatement.condition(), whileStatement.body());
        }
    }

    private void visitLoop(final SourcePosition sourcePos, final Expression condition, final Statement body) {
        try {
            visit(condition);
            RuntimeType condType = condition.getType().baseType();
            if (!(condType instanceof PrimType.BoolType)) {
                errors.add(new SemanticException.TypeError(sourcePos, condType, BOOL_TYPE));
            }
        } catch (SemanticException e) {
            errors.add(e);
        }

        visit(body);
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
    record Binding(RuntimeType type, boolean constant, Declaration declaration) {

        Binding(RuntimeType type) {
            this(type, true, null);
        }

    }

}
