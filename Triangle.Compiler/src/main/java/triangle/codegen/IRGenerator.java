package triangle.codegen;

import triangle.abstractMachine.Machine;
import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;
import triangle.repr.Argument;
import triangle.repr.Declaration;
import triangle.repr.Expression;
import triangle.repr.Instruction;
import triangle.repr.Parameter;
import triangle.repr.Parameter.VarParameter;
import triangle.repr.Statement;
import triangle.repr.Type;
import triangle.util.SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// TODO: tests for IRGen
// TODO: when we know that an identifier's location is statically derivable (does not contain array with non-constant
//  subscript) we can special-case the generateStore/Fetch methods to be more efficient
public class IRGenerator {

    static final Map<String, Callable> builtins = new HashMap<>();

    static {
        // TODO: fill up
        // TODO: StdEnv should have a Map<String, Primitive> and we should build callables from it here
        // TAM primitives
        builtins.put("=", new Callable.PrimitiveCallable(Primitive.EQ));
        builtins.put("<", new Callable.PrimitiveCallable(Primitive.LT));
        builtins.put(">", new Callable.PrimitiveCallable(Primitive.GT));
        builtins.put(">=", new Callable.PrimitiveCallable(Primitive.GE));
        builtins.put("<=", new Callable.PrimitiveCallable(Primitive.LE));
        builtins.put("-", new Callable.PrimitiveCallable(Primitive.SUB));
        builtins.put("+", new Callable.PrimitiveCallable(Primitive.ADD));
        builtins.put("*", new Callable.PrimitiveCallable(Primitive.MULT));
        builtins.put("/", new Callable.PrimitiveCallable(Primitive.DIV));
        builtins.put("\\", new Callable.PrimitiveCallable(Primitive.NOT));
        builtins.put("/\\", new Callable.PrimitiveCallable(Primitive.AND));
        builtins.put("\\/", new Callable.PrimitiveCallable(Primitive.OR));
        builtins.put("//", new Callable.PrimitiveCallable(Primitive.MOD));
        builtins.put("getint", new Callable.PrimitiveCallable(Primitive.GETINT));
        builtins.put("put", new Callable.PrimitiveCallable(Primitive.PUT));
        builtins.put("putint", new Callable.PrimitiveCallable(Primitive.PUTINT));
        builtins.put("puteol", new Callable.PrimitiveCallable(Primitive.PUTEOL));
        builtins.put("eol", new Callable.PrimitiveCallable(Primitive.EOL));
        builtins.put("get", new Callable.PrimitiveCallable(Primitive.GET));
        builtins.put("\\=", new Callable.PrimitiveCallable(Primitive.NE));
        builtins.put("geteol", new Callable.PrimitiveCallable(Primitive.GETEOL));

        // Compiler generated operations (++, --, etc)
        // |
        builtins.put("|", new Callable.CompilerGenerated(List.of(
                new Instruction.LOADL(100),
                Instruction.TAMInstruction.callPrim(Primitive.MULT)
        )));
    }

    static Register getDisplayRegister(final int depth) {
        return switch (depth) {
            case 0 -> Register.LB;
            case 1 -> Register.L1;
            case 2 -> Register.L2;
            case 3 -> Register.L3;
            case 4 -> Register.L4;
            case 5 -> Register.L5;
            case 6 -> Register.L6;
            default -> throw new RuntimeException("static nesting-depth limit exceeded");
        };
    }

    private final SymbolTable<Callable, Void>   funcAddresses = new SymbolTable<>(builtins, null);
    private final SymbolTable<Integer, Integer> localVars     = new SymbolTable<>(0);
    private final Supplier<Instruction.LABEL>   labelSupplier = new Supplier<>() {
        private int i = 0;

        @Override public Instruction.LABEL get() {
            return new Instruction.LABEL(i++);
        }
    };

    public List<Instruction> generateIR(Statement statement) {
        List<Instruction> ir = generate(statement);
        ir.add(new Instruction.HALT());
        return ir;
    }

    // the non-trivial code-generation bits below have comments, in following format, explaining the generated code:
    //      OPCODE (n) d[r]     is used as in the TAM specification
    //      [expression]               denotes the result of generating code for `expression`

    private List<Instruction> generate(Statement statement) {
        List<Instruction> block = new ArrayList<>();

        switch (statement) {
            case Statement.ExpressionStatement expressionStatement -> {
                //  [expression]
                //  POP 0 resultSize

                block.addAll(generate(expressionStatement.expression()));

                // discard results of ExpressionStatements, since they are intended to be used only for side-effects
                int resultSize = expressionStatement.expression().getType().size();
                if (resultSize > 0) {
                    block.add(new Instruction.POP(0, resultSize));
                }
                return block;
            }
            case Statement.AssignStatement assignStatement -> {
                //  [expression]
                //  evaluateAddress(variable)
                //  STOREI varSize

                // evaluate expression
                block.addAll(generate(assignStatement.expression()));
                // evaluate address of identifier and put on stack
                block.addAll(generateStore(assignStatement.identifier(), assignStatement.expression().getType().size()));
                return block;
            }
            case Statement.IfStatement ifStatement -> {
                //  [condition]
                //  JUMPIF 0 altLabel
                //  [consequent]
                //  JUMP skipLabel
                // altLabel:
                //  [alternative]
                // skipLabel:

                Instruction.LABEL altLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                block.addAll(generate(ifStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.falseRep, altLabel));
                ifStatement.consequent().ifPresent(s -> block.addAll(generate(s)));
                block.add(new Instruction.JUMP_LABEL(skipLabel));
                block.add(altLabel);
                ifStatement.alternative().ifPresent(s -> block.addAll(generate(s)));
                block.add(skipLabel);
                return block;
            }
            case Statement.LetStatement letStatement -> {
                //  allocateDeclarations(declarations)
                //  [statement]

                // as declarations are evaluated, they will expand the stack
                // store the current stack top so we know how much to POP when returning
                int savedStackOffset = localVars.scopeLocalState();
                int newStackOffset = allocateDeclarations(block, letStatement.declarations());
                int totalAllocated = newStackOffset - savedStackOffset;

                // set the scope local state to the new stack top so that any let statements/expressions in let body know how
                // much to POP when returning
                localVars.setScopeLocalState(newStackOffset);

                // let body is generated in the new scope containing all our definitions
                block.addAll(generate(letStatement.statement()));

                if (totalAllocated > 0) {
                    block.add(new Instruction.POP(0, totalAllocated));
                }

                return block;
            }
            case Statement.LoopWhileStatement loopWhileStatement -> {
                // loopLabel:
                //  [loopBody]
                //  [condition]
                //  JUMPIF 0 skipLabel
                //  [doBody]
                //  JUMP loopLabel
                // skipLabel:

                Instruction.LABEL loopLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                block.add(loopLabel);
                block.addAll(generate(loopWhileStatement.loopBody()));
                block.addAll(generate(loopWhileStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.falseRep, skipLabel));
                block.addAll(generate(loopWhileStatement.doBody()));
                block.add(new Instruction.JUMP_LABEL(loopLabel));
                block.add(skipLabel);

                return block;
            }
            case Statement.RepeatUntilStatement repeatUntilStatement -> {
                Instruction.LABEL loopLabel = labelSupplier.get();
                // loopLabel:
                //  [body]
                //  [condition]
                //  JUMPIF 0 loopLabel

                block.add(loopLabel);
                block.addAll(generate(repeatUntilStatement.body()));
                block.addAll(generate(repeatUntilStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.falseRep, loopLabel));

                return block;
            }
            case Statement.RepeatWhileStatement repeatWhileStatement -> {
                Instruction.LABEL loopLabel = labelSupplier.get();
                // loopLabel:
                //  [body]
                //  [condition]
                //  JUMPIF 1 loopLabel

                block.add(loopLabel);
                block.addAll(generate(repeatWhileStatement.body()));
                block.addAll(generate(repeatWhileStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.trueRep, loopLabel));

                return block;
            }
            case Statement.StatementBlock statementBlock -> {
                //  [statements(1)]
                //  [statements(2)]
                //  ...
                //  [statements(n)]

                for (Statement aStatement : statementBlock.statements()) {
                    block.addAll(generate(aStatement));
                }

                return block;
            }
            case Statement.WhileStatement whileStatement -> {
                // loopLabel:
                //  [condition]
                //  JUMPIF 0 skipLabel
                //  [body]
                //  JUMP loopLabel
                // skipLabel:

                Instruction.LABEL loopLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                block.add(loopLabel);
                block.addAll(generate(whileStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.falseRep, skipLabel));
                block.addAll(generate(whileStatement.body()));
                block.add(new Instruction.JUMP_LABEL(loopLabel));
                block.add(skipLabel);

                return block;
            }
        }
    }

    private List<Instruction> generate(Expression expression) {
        List<Instruction> block = new ArrayList<>();

        switch (expression) {
            case Expression.BinaryOp binaryOp -> {
                //  generateCall(op, [lOperand, rOperand])

                return generateCall(
                        binaryOp.operator().name(),
                        List.of(binaryOp.leftOperand(), binaryOp.rightOperand())
                );
            }
            case Expression.FunCall funCall -> {
                // generateCall(func, func.arguments)

                return generateCall(funCall.func().name(), funCall.arguments());
            }
            case Expression.Identifier identifier -> {
                // fetch the value and leave on stack
                return generateFetch(identifier, identifier.getType().baseType().size());
            }
            case Expression.IfExpression ifExpression -> {
                //  [condition]
                //  JUMPIF 0 altLabel
                //  [consequent]
                //  JUMP skipLabel
                // alternateLabel:
                //  [alternative]
                // skipLabel:

                Instruction.LABEL altLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                block.addAll(generate(ifExpression.condition()));
                block.add(new Instruction.JUMPIF_LABEL(Machine.falseRep, altLabel));
                block.addAll(generate(ifExpression.consequent()));
                block.add(new Instruction.JUMP_LABEL(skipLabel));
                block.add(altLabel);
                block.addAll(generate(ifExpression.alternative()));
                block.add(skipLabel);
                return block;
            }
            case Expression.LetExpression letExpression -> {
                // see generate(LetStatement) for comments

                int savedStackOffset = localVars.scopeLocalState();
                int newStackOffset = allocateDeclarations(block, letExpression.declarations());
                int totalAllocated = newStackOffset - savedStackOffset;

                localVars.setScopeLocalState(newStackOffset);

                block.addAll(generate(letExpression.expression()));

                int resultSize = letExpression.expression().getType().size();

                if (totalAllocated > 0) {
                    block.add(new Instruction.POP(resultSize, totalAllocated));
                }

                return block;
            }
            case Expression.LitArray litArray -> {
                // generate all the values and leave them on the stack contiguously
                for (Expression e : litArray.elements()) {
                    block.addAll(generate(e));
                }
                return block;
            }
            case Expression.LitBool litBool -> {
                block.add(new Instruction.LOADL(litBool.value() ? Machine.trueRep : Machine.falseRep));
                return block;
            }
            case Expression.LitChar litChar -> {
                block.add(new Instruction.LOADL(litChar.value()));
                return block;
            }
            case Expression.LitInt litInt -> {
                block.add(new Instruction.LOADL(litInt.value()));
                return block;
            }
            case Expression.LitRecord litRecord -> {
                for (Expression.LitRecord.RecordField field : litRecord.fields()) {
                    block.addAll(generate(field.value()));
                }
                return block;
            }
            case Expression.UnaryOp unaryOp -> {
                // generateCall(op, [operand])
                block.addAll(generateCall(unaryOp.operator().name(), List.of(unaryOp.operand())));
                return block;
            }
            case Expression.SequenceExpression sequenceExpression -> {
                // [statement]
                // [expression]

                block.addAll(generate(sequenceExpression.statement()));
                block.addAll(generate(sequenceExpression.expression()));
                return block;
            }
        }
    }

    private List<Instruction> generateCall(final String funcName, final List<Argument> arguments) {
        List<Instruction> block = new ArrayList<>();

        //  [argument(0)]
        //  [argument(1)]
        //  ...
        //  [argument(n)]

        for (Argument argument : arguments) {
            switch (argument) {
                case Argument.FuncArgument funcArgument -> {
                    // put a closure -- static link + code address -- onto the stack

                    SymbolTable<Callable, Void>.DepthLookup lookup = funcAddresses.lookupWithDepth(funcArgument.func().name());
                    Register nonLocalsLink = getDisplayRegister(lookup.depth());

                    switch (lookup.t()) {
                        case Callable.DynamicCallable(int stackOffset) -> {
                            //  LOADA stackOffset[nonLocalsLink]         <- load static link
                            //  LOADI Machine.addressSize
                            //  LOADA (stackOffset + 1)[nonLocalsLink]   <- load code addr
                            //  LOADI Machine.addressSize

                            // closure (i.e, func and proc arguments) are addresses to addresses, so dereference
                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, stackOffset)));
                            block.add(new Instruction.LOADI(Machine.addressSize));
                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, stackOffset + 1)));
                            block.add(new Instruction.LOADI(Machine.addressSize));
                        }
                        case Callable.PrimitiveCallable(Primitive primitive) -> {
                            //  LOADA 0[LB]
                            //  LOADA primitive.ordinal()[PB]

                            // use whatever as static link for primitives -- 0[LB] here
                            block.add(new Instruction.LOADA(new Instruction.Address(Register.LB, 0)));
                            block.add(new Instruction.LOADA(new Instruction.Address(Register.PB, primitive.ordinal())));
                        }
                        case Callable.StaticCallable(Instruction.LABEL label) -> {
                            //  LOADA 0[nonLocalsLink]
                            //  LOADA label

                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, 0)));
                            block.add(new Instruction.LOADA_LABEL(label));
                        }
                        // [instructions]
                        case Callable.CompilerGenerated(List<Instruction> instructions) -> block.addAll(instructions);
                    }
                }
                // load address of var argument
                // if the argument provided is already an address, then dont dereference it here needlessly
                case Argument.VarArgument varArgument -> block.addAll(
                        generateRuntimeLocation(varArgument.var(), varArgument.getType() instanceof Type.RefOf));
                // just evaluate expr and leave it on stack
                case Expression expressionArg -> block.addAll(generate(expressionArg));
            }
        }

        // we have to special case chr and ord; TAM cannot provide primitives for these because it doesn't know our char encoding
        if (funcName.equals("chr") || funcName.equals("ord")) {
            // the argument was generated and already on the stack, so just return; SemanticAnalyzer ensures that nothing can
            // go wrong if we just leave the value as is, because our char encoding translated without changes to
            // our integer encoding
            return block;
        }

        // eq and neq take an additional size argument that is not visible to the user (i.e., it does not and should not show
        // up on semantic analysis and the user must not have to add the size argument manually)
        if (funcName.equals("=") || funcName.equals("\\=")) {
            // push size on the stack
            block.add(new Instruction.LOADL(arguments.getFirst().getType().baseType().size()));
        }

        SymbolTable<Callable, Void>.DepthLookup lookup = funcAddresses.lookupWithDepth(funcName);
        switch (lookup.t()) {
            case Callable.DynamicCallable(int stackOffset) -> {
                //  LOAD addressSize stackOffset[nonLocalsLink]          <- load static link
                //  LOAD addressSize (stackOffset + 1)[nonLocalsLink]    <- load code addr
                //  CALLI

                Register nonLocalsLink = getDisplayRegister(lookup.depth());
                block.add(new Instruction.LOAD(Machine.addressSize, new Instruction.Address(nonLocalsLink, stackOffset)));
                block.add(new Instruction.LOAD(Machine.addressSize, new Instruction.Address(nonLocalsLink, stackOffset + 1)));
                block.add(new Instruction.CALLI());
            }
            case Callable.PrimitiveCallable(Primitive primitive) -> block.add(Instruction.TAMInstruction.callPrim(primitive));
            case Callable.StaticCallable(Instruction.LABEL label) -> block.add(
                    new Instruction.CALL_LABEL(getDisplayRegister(lookup.depth()), label));
            case Callable.CompilerGenerated(List<Instruction> instructions) -> block.addAll(instructions);
        }

        return block;
    }

    // allocate space for each declaration, pushing to the stack as needed
    // when procedures/functions are encountered, the code for the function is generated in-place but a jump-ahead is included
    // so that it is not executed immediately; a label is associated with the entry point of the newly defined function and
    // added to funcAddresses
    private int allocateDeclarations(final List<Instruction> block, final List<Declaration> declarations) {
        int stackOffset = localVars.scopeLocalState();

        for (Declaration declaration : declarations) {
            switch (declaration) {
                case Declaration.ConstDeclaration constDeclaration -> {
                    // generate value -> declaration name with stack offset -> bump stack offset by size of declared value
                    block.addAll(generate(constDeclaration.value()));
                    localVars.add(constDeclaration.name(), stackOffset);
                    stackOffset += constDeclaration.value().getType().size();
                }
                case Declaration.FuncDeclaration funcDeclaration -> {
                    //  JUMP skipLabel
                    // funcLabel:
                    //  [functionBody]
                    //  RETURN returnValueSize paramsSize
                    // skipLabel:

                    //noinspection DuplicatedCode
                    Instruction.LABEL skipLabel = labelSupplier.get();
                    Instruction.LABEL funcLabel = labelSupplier.get();

                    block.add(new Instruction.JUMP_LABEL(skipLabel));
                    block.add(funcLabel);

                    // the function currently being declared be added to funcAddresses before we process its declaration, so as
                    // to allow recursion
                    funcAddresses.add(funcDeclaration.name(), new Callable.StaticCallable(funcLabel));

                    // new scope for local vars and functions, since we are in a func declaration, the scope local state (i.e.,
                    // the initial stack offset) must be Machine.linkDataSize -- for static link, dynamic link, return address
                    localVars.enterNewScope(Machine.linkDataSize);
                    funcAddresses.enterNewScope(null);

                    // we need the total amount of space taken by the function to know what to use for the RETURN call
                    int paramsSize = addParametersToScope(funcDeclaration.parameters());

                    block.addAll(generate(funcDeclaration.expression()));
                    block.add(new Instruction.RETURN(funcDeclaration.expression().getType().size(), paramsSize));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    block.add(skipLabel);
                }
                // nothing to do for type declarations
                case Declaration.TypeDeclaration _ -> { }
                case Declaration.VarDeclaration varDeclaration -> {
                    block.add(new Instruction.PUSH(varDeclaration.getType().size()));
                    localVars.add(varDeclaration.name(), stackOffset);
                    stackOffset += varDeclaration.getType().size();
                }
                case Declaration.ProcDeclaration procDeclaration -> {
                    //  JUMP skipLabel
                    // procLabel:
                    //  [procBody]
                    //  RETURN 0 paramsSize
                    // skipLabel:

                    //noinspection DuplicatedCode
                    Instruction.LABEL skipLabel = labelSupplier.get();
                    Instruction.LABEL procLabel = labelSupplier.get();

                    block.add(new Instruction.JUMP_LABEL(skipLabel));
                    block.add(procLabel);

                    // the proc currently being declared must be added to funcAddresses before we process its declaration, so
                    // as to allow recursion
                    funcAddresses.add(procDeclaration.name(), new Callable.StaticCallable(procLabel));

                    // new scope for local vars and functions, since we are in a proc declaration, the scope local state (i.e.,
                    // the initial stack offset) must be Machine.linkDataSize -- for static link, dynamic link, return address
                    localVars.enterNewScope(Machine.linkDataSize);
                    funcAddresses.enterNewScope(null);

                    // we need the total amount of space that will be taken by the parameters to know how to RETURN
                    int paramsSize = addParametersToScope(procDeclaration.parameters());

                    block.addAll(generate(procDeclaration.statement()));
                    block.add(new Instruction.RETURN(0, paramsSize));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    block.add(skipLabel);
                }
            }
        }

        return stackOffset;
    }

    // associates each parameter with its location relative to current frame, returns no words that will be  allocated for
    // parameter when the corresponding function is called
    private int addParametersToScope(List<Parameter> parameters) {
        int paramOffset = 0;
        for (Parameter parameter : parameters.reversed()) {
            paramOffset -= parameter.getType().size();
            switch (parameter) {
                // static link and code address to be on stack
                case Parameter.FuncParameter funcParameter -> funcAddresses.add(
                        funcParameter.name(),
                        new Callable.DynamicCallable(paramOffset)
                );
                case Parameter.ValueParameter valueParameter -> localVars.add(
                        valueParameter.name(), paramOffset);
                case VarParameter varParameter -> localVars.add(varParameter.name(), paramOffset);
            }
        }

        return paramOffset * -1;
    }

    // generates instructions to pop the last `size` words of data and store it to the location that identifier will be found in
    // at runtime
    private List<Instruction> generateStore(Expression.Identifier identifier, int size) {
        List<Instruction> block = new ArrayList<>();

        switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                block.addAll(generateRuntimeLocation(arraySubscript, true));
                block.add(new Instruction.STOREI(size));
                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                Instruction.Address address = lookupAddress(basicIdentifier);

                if (basicIdentifier.getType() instanceof Type.RefOf) {
                    block.addAll(generateRuntimeLocation(identifier, true));
                    block.add(new Instruction.STOREI(size));
                } else {
                    block.add(new Instruction.STORE(size, address));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> {
                block.addAll(generateRuntimeLocation(recordAccess, true));
                block.add(new Instruction.STOREI(size));
                return block;
            }
        }
    }

    // generates instructions to push the address of `identifier`s runtime location to the stack; if dereferencing is true, the
    // address is dereferenced iff it is a RefOf
    private List<Instruction> generateRuntimeLocation(Expression.Identifier identifier, boolean dereferencing) {
        List<Instruction> block = new ArrayList<>();

        switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                // push address of array base on stack
                block.addAll(generateRuntimeLocation(arraySubscript.array(), dereferencing));
                // evaluate subscript
                block.addAll(generate(arraySubscript.subscript()));
                // LOADL arrayElementSize
                // push element size on stack
                block.add(new Instruction.LOADL(
                        ((Type.ArrayType) arraySubscript.array().getType().baseType()).elementType().size()));
                // CALL Primitive.MULT, to get offset
                block.add(Instruction.TAMInstruction.callPrim(Primitive.MULT));
                // CALL Primitive.ADD, to add offset to address of root
                block.add(Instruction.TAMInstruction.callPrim(Primitive.ADD));
                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                Instruction.Address address = lookupAddress(basicIdentifier);
                block.add(new Instruction.LOADA(address));

                // if its a identifier is already a reference, then "dereference" it
                if (dereferencing && basicIdentifier.getType() instanceof Type.RefOf) {
                    block.add(new Instruction.LOADI(Machine.addressSize));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> {
                block.addAll(generateRuntimeLocation(recordAccess.record(), dereferencing));
                block.addAll(generateRecordAccess(recordAccess));
                return block;
            }
        }
    }

    // TODO: simplify record access
    private List<Instruction> generateRecordAccess(Expression.Identifier.RecordAccess recordAccess) {
        List<Instruction> block = new ArrayList<>();

        // first generate instructions to access the base of the record; this can be any arbitrary identifier
        switch (recordAccess.record()) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                Type.ArrayType arrayType = (Type.ArrayType) arraySubscript.array().getType().baseType();
                int elemSize = arrayType.elementType().baseType().size();

                block.addAll(generate(arraySubscript.subscript()));
                block.add(new Instruction.LOADL(elemSize));
                block.add(Instruction.TAMInstruction.callPrim(Primitive.MULT));
                block.add(Instruction.TAMInstruction.callPrim(Primitive.ADD));
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> { }
            // our parser guarantees this
            case Expression.Identifier.RecordAccess access -> throw new RuntimeException("record access as left-side of record");
        }

        // calculate the offset from base address of root, to the place where the field we want to access starts
        // this assumes that order of field types corresponds to order in which field values are located contiguously
        // in memory
        int offset = 0;
        var fieldTypes = ((Type.RecordType) recordAccess.record().getType().baseType()).fieldTypes();
        for (var fieldType : fieldTypes) {
            if (fieldType.fieldName().equals(recordAccess.field().root().name())) {
                break;
            }

            assert !(fieldType.fieldType() instanceof Type.RefOf);
            offset += fieldType.fieldType().size();
        }

        // bump address on the stack by `offset`, if needed
        if (offset > 0) {
            block.add(new Instruction.LOADL(offset));
            block.add(Instruction.TAMInstruction.callPrim(Primitive.ADD));
        }

        // now, the top of the stack contains the base address for record.accessedField

        // depending on what kind of identifier the field is, we want to do a few different things
        switch (recordAccess.field()) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                Type.ArrayType arrayType = (Type.ArrayType) arraySubscript.array().getType().baseType();
                int elemSize = arrayType.elementType().baseType().size();

                block.addAll(generate(arraySubscript.subscript()));
                block.add(new Instruction.LOADL(elemSize));
                block.add(Instruction.TAMInstruction.callPrim(Primitive.MULT));
                block.add(Instruction.TAMInstruction.callPrim(Primitive.ADD));

                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                return block;
            }
            case Expression.Identifier.RecordAccess access -> {
                block.addAll(generateRecordAccess(access));
                return block;
            }
        }
    }

    // generates instructions to push the value in `identifier`'s runtime location to the stack
    private List<Instruction> generateFetch(Expression.Identifier identifier, int size) {
        List<Instruction> block = new ArrayList<>();
        switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                block.addAll(generateRuntimeLocation(arraySubscript, true));
                block.add(new Instruction.LOADI(size));
                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                Instruction.Address address = lookupAddress(basicIdentifier);

                if (basicIdentifier.getType() instanceof Type.RefOf) {
                    block.addAll(generateRuntimeLocation(identifier, true));
                    block.add(new Instruction.LOADI(size));
                } else {
                    block.add(new Instruction.LOAD(size, address));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> {
                block.addAll(generateRuntimeLocation(recordAccess.record(), true));
                block.addAll(generateRecordAccess(recordAccess));
                block.add(new Instruction.LOADI(size));
                return block;
            }
        }
    }

    // given a basic identifier, gets the address it may be found in at runtime
    private Instruction.Address lookupAddress(Expression.Identifier.BasicIdentifier basicIdentifier) {
        SymbolTable<Integer, Integer>.DepthLookup lookup = localVars.lookupWithDepth(basicIdentifier.name());
        return new Instruction.Address(getDisplayRegister(lookup.depth()), lookup.t());
    }

    // represents things that may be the target of CALL/CALLI instructions
    sealed interface Callable
            permits Callable.CompilerGenerated, Callable.DynamicCallable, Callable.PrimitiveCallable, Callable.StaticCallable {

        // a callable whose location is known statically
        record StaticCallable(Instruction.LABEL label) implements Callable { }

        // a callable whose closure may be found at the given stackOffset with static nesting depth decided elsewhere
        record DynamicCallable(int stackOffset) implements Callable { }

        // a primitive call
        record PrimitiveCallable(Primitive primitive) implements Callable { }

        // TODO: we shouldnt generate instructions repeatedly like this; have a List<Instruction> of compiler generated
        //  routines and use StaticCallable instead
        // a sequence of compiler generated instructions which can directly be inlined
        @Deprecated record CompilerGenerated(List<Instruction> instructions) implements Callable { }

    }

}
