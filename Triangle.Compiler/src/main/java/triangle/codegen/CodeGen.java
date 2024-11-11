package triangle.codegen;

import triangle.abstractMachine.Machine;
import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Parameter.VarParameter;
import triangle.ast.Statement;
import triangle.ast.RuntimeType;
import triangle.util.SymbolTable;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: move write() and backpatch() to separate class
// TODO: verify static links work as expected
public class CodeGen {

    private static final Map<String, Callable> primitives = new HashMap<>();

    static {
        // TODO: fill up
        primitives.put(">", new Callable.PrimitiveCallable(Primitive.GT));
        primitives.put(">=", new Callable.PrimitiveCallable(Primitive.GE));
        primitives.put("-", new Callable.PrimitiveCallable(Primitive.SUB));
        primitives.put("+", new Callable.PrimitiveCallable(Primitive.ADD));
        primitives.put("putint", new Callable.PrimitiveCallable(Primitive.PUTINT));
        primitives.put("puteol", new Callable.PrimitiveCallable(Primitive.PUTEOL));
    }

    // TODO: SemanticAnalyzer should ensure static-nesting depth does not exceed the maximum
    private static Register getDisplayRegister(final int depth) {
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

    private static void write(final List<Instruction> instructions) {
        try (DataOutputStream fw = new DataOutputStream(new FileOutputStream("obj.tam"))) {
            for (Instruction instruction : instructions) {
                // TODO: this is too ugly to stay + fill in
                switch (instruction) {
                    case Instruction.CALL call -> {
                        fw.writeInt(6);
                        fw.writeInt(call.address().r().ordinal());
                        fw.writeInt(call.staticLink().ordinal());
                        fw.writeInt(call.address().d());
                    }
                    case Instruction.CALLI calli -> {
                        fw.writeInt(7);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(0);
                    }
                    case Instruction.HALT halt -> {
                        fw.writeInt(15);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(0);
                    }
                    case Instruction.JUMP jump -> {
                        fw.writeInt(12);
                        fw.writeInt(jump.address().r().ordinal());
                        fw.writeInt(0);
                        fw.writeInt(jump.address().d());
                    }
                    case Instruction.JUMPIF jumpif -> {
                        fw.writeInt(14);
                        fw.writeInt(jumpif.address().r().ordinal());
                        fw.writeInt(jumpif.value());
                        fw.writeInt(jumpif.address().d());
                    }
                    case Instruction.LOAD load -> {
                        fw.writeInt(0);
                        fw.writeInt(load.address().r().ordinal());
                        fw.writeInt(load.words());
                        fw.writeInt(load.address().d());
                    }
                    case Instruction.LOADA loada -> {
                        fw.writeInt(1);
                        fw.writeInt(loada.address().r().ordinal());
                        fw.writeInt(0);
                        fw.writeInt(loada.address().d());
                    }
                    case Instruction.LOADI loadi -> {
                        fw.writeInt(2);
                        fw.writeInt(0);
                        fw.writeInt(loadi.size());
                        fw.writeInt(0);
                    }
                    case Instruction.LOADL loadl -> {
                        fw.writeInt(3);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(loadl.value());
                    }
                    case Instruction.POP pop -> {
                        fw.writeInt(11);
                        fw.writeInt(0);
                        fw.writeInt(pop.resultWords());
                        fw.writeInt(pop.popCount());
                    }
                    case Instruction.PUSH push -> {
                        fw.writeInt(10);
                        fw.writeInt(0);
                        fw.writeInt(0);
                        fw.writeInt(push.words());
                    }
                    case Instruction.RETURN aReturn -> {
                        fw.writeInt(8);
                        fw.writeInt(0);
                        fw.writeInt(aReturn.resultSize());
                        fw.writeInt(aReturn.argsSize());
                    }
                    case Instruction.STORE store -> {
                        fw.writeInt(4);
                        fw.writeInt(store.address().r().ordinal());
                        fw.writeInt(store.words());
                        fw.writeInt(store.address().d());
                    }
                    case Instruction.STOREI storei -> {
                        fw.writeInt(5);
                        fw.writeInt(0);
                        fw.writeInt(storei.size());
                        fw.writeInt(0);
                    }
                    case Instruction.CALL_LABEL _,
                         Instruction.CALL_PRIM _,
                         Instruction.JUMPIF_LABEL _,
                         Instruction.JUMP_LABEL _,
                         Instruction.LABEL _,
                         Instruction.LOADA_LABEL _ -> throw new RuntimeException("attempted write of pseudo-instruction");
                }
            }
        } catch (IOException e) {
            System.err.println("failed write: " + e.getMessage());
        }
    }

    // TODO: maybe do some optimizations before backpatching? eg. any label immediately followed by a jump can just edit all jumps
    //  to that label
    // backpatch the instruction list to resolve all labels, etc.
    private static List<Instruction> backpatch(final List<Instruction> instructions) {
        Map<Instruction.LABEL, Integer> labelLocations = new HashMap<>();

        int offset = 0;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            if (instruction instanceof Instruction.LABEL label) {
                labelLocations.put(label, i - offset);
                offset += 1;
            }
        }

        // so I don't have to type 'new Address ...' repeatedly
        Function<Instruction.LABEL, Instruction.Address> toCodeAddress = label -> new Instruction.Address(
                Register.CB,
                labelLocations.get(
                        label)
        );

        // TODO: this also patches calls to CALL_PRIM, but that should probably be done in another method
        List<Instruction> patchedInstructions = new ArrayList<>();
        for (Instruction instruction : instructions) {
            switch (instruction) {
                case Instruction.LABEL _ -> { }
                case Instruction.CALL_LABEL(Register staticLink, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.CALL(staticLink, toCodeAddress.apply(label)));
                case Instruction.JUMPIF_LABEL(int value, Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMPIF(value, toCodeAddress.apply(label)));
                case Instruction.JUMP_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.JUMP(toCodeAddress.apply(label)));
                case Instruction.LOADA_LABEL(Instruction.LABEL label) -> patchedInstructions.add(
                        new Instruction.LOADA(toCodeAddress.apply(label)));
                case Instruction.CALL_PRIM(Primitive primitive) ->
                    // use whatever as static link for primitive
                    // 0'th primitive in Primitive corresponds to address PB + 1
                        patchedInstructions.add(
                                new Instruction.CALL(Register.SB, new Instruction.Address(Register.PB, primitive.ordinal())));
                default -> patchedInstructions.add(instruction);
            }
        }

        return patchedInstructions;
    }

    private final SymbolTable<Callable, Void>   funcAddresses = new SymbolTable<>(primitives, null);
    private final SymbolTable<Integer, Integer> localVars     = new SymbolTable<>(0);
    private final Supplier<Instruction.LABEL>   labelSupplier = new Supplier<>() {
        private int i = 0;

        @Override public Instruction.LABEL get() {
            return new Instruction.LABEL(i++);
        }
    };

    public List<Instruction> compile(Statement statement) {
        // TODO: refactor
        List<Instruction> unpatched = generate(statement);
        unpatched.add(new Instruction.HALT());
        unpatched.forEach(System.out::println);
        System.out.println("====");
        var x = backpatch(unpatched);
        for (int i = 0; i < x.size(); i++) {
            System.out.println(i + ":\t" + x.get(i));
        }
        write(x);
        return x;
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
                //  JUMPIF 0 skipLabel
                //  [consequent]
                // skipLabel:
                //  [alternative]

                Instruction.LABEL skipLabel = labelSupplier.get();

                block.addAll(generate(ifStatement.condition()));
                block.add(new Instruction.JUMPIF_LABEL(0, skipLabel));

                if (ifStatement.consequent().isPresent()) {
                    block.addAll(generate(ifStatement.consequent().get()));
                }
                block.add(skipLabel);

                if (ifStatement.alternative().isPresent()) {
                    block.addAll(generate(ifStatement.alternative().get()));
                }

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
                block.add(new Instruction.JUMPIF_LABEL(0, skipLabel));
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
                block.add(new Instruction.JUMPIF_LABEL(0, loopLabel));

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
                block.add(new Instruction.JUMPIF_LABEL(1, loopLabel));

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
                block.add(new Instruction.JUMPIF_LABEL(0, skipLabel));
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
                //  [lOperand]
                //  [rOperand]
                //  generateCall(op)

                block.addAll(generateCall(
                        binaryOp.operator().name(),
                        List.of(binaryOp.leftOperand(), binaryOp.rightOperand())
                ));
                return block;
            }
            case Expression.FunCall funCall -> {
                //  [arguments(1)]
                //  [arguments(2)]
                //  ...
                //  [arguments(n)]
                //  CALL/CALLI/CALL_PRIM -- depending on the type of function called

                block.addAll(generateCall(funCall.func().name(), funCall.arguments()));
                return block;
            }
            case Expression.Identifier identifier -> {
                // fetch the value and leave on stack
                block.addAll(generateFetch(identifier, identifier.getType().baseType().size()));
                return block;
            }
            case Expression.IfExpression ifExpression -> throw new RuntimeException();
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
            // the fields in a lit record can be declared in any order?
            case Expression.LitRecord litRecord -> throw new RuntimeException();
            case Expression.UnaryOp unaryOp -> throw new RuntimeException();
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
                            //  LOADA stackOffset[nonLocalsLink]         <- load code address
                            //  LOADA (stackOffset - 1)[nonLocalsLink]   <- load static link

                            // the `CALLI` instruction expects static link to be on top, followed by code address

                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, stackOffset)));
                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, stackOffset - 1)));
                        }
                        case Callable.PrimitiveCallable(Primitive primitive) -> {
                            //  LOADA primitive.ordinal()[PB]
                            //  LOADA 0[LB]

                            block.add(new Instruction.LOADA(new Instruction.Address(Register.PB, primitive.ordinal())));
                            // use whatever as static link for primitives -- 0[LB] here
                            block.add(new Instruction.LOADA(new Instruction.Address(Register.LB, 0)));
                        }
                        case Callable.StaticCallable(Instruction.LABEL label) -> {
                            //  LOADA label
                            //  LOADA 0[nonLocalsLink]

                            block.add(new Instruction.LOADA_LABEL(label));
                            block.add(new Instruction.LOADA(new Instruction.Address(nonLocalsLink, 0)));
                        }
                    }
                }
                // load address of var argument
                // if the argument provided is already an address, then dont dereference it here needlessly
                case Argument.VarArgument varArgument -> block.addAll(
                        generateRuntimeLocation(varArgument.var(), varArgument.getType() instanceof RuntimeType.RefOf));
                // just evaluate expr and leave it on stack
                case Expression expressionArg -> block.addAll(generate(expressionArg));
            }
        }

        SymbolTable<Callable, Void>.DepthLookup lookup = funcAddresses.lookupWithDepth(funcName);
        Register nonLocalsLink = getDisplayRegister(lookup.depth());

        switch (lookup.t()) {
            case Callable.DynamicCallable(int stackOffset) -> {
                //  LOAD addressSize stackOffset[nonLocalsLink]          <- load code address
                //  LOAD addressSize (stackOffset - 1)[nonLocalsLink]    <- load static link
                //  CALLI

                block.add(new Instruction.LOAD(Machine.addressSize, new Instruction.Address(nonLocalsLink, stackOffset)));
                block.add(new Instruction.LOAD(Machine.addressSize, new Instruction.Address(nonLocalsLink, stackOffset - 1)));
                block.add(new Instruction.CALLI());
            }
            case Callable.PrimitiveCallable(Primitive primitive) -> block.add(new Instruction.CALL_PRIM(primitive));
            case Callable.StaticCallable(Instruction.LABEL label) -> block.add(new Instruction.CALL_LABEL(nonLocalsLink, label));
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

                    Instruction.LABEL skipLabel = labelSupplier.get();
                    Instruction.LABEL funcLabel = labelSupplier.get();

                    block.add(new Instruction.JUMP_LABEL(skipLabel));
                    block.add(funcLabel);

                    // new scope for local vars and functions
                    localVars.enterNewScope(0);
                    funcAddresses.enterNewScope(null);

                    // the function currently being declared must be visible in its own definition, so as to allow recursion
                    funcAddresses.add(funcDeclaration.name(), new Callable.StaticCallable(funcLabel));

                    // we need the total amount of space taken by the function to know what to use for the RETURN call
                    int paramsSize = addParametersToScope(funcDeclaration.parameters());

                    block.addAll(generate(funcDeclaration.expression()));
                    block.add(new Instruction.RETURN(funcDeclaration.expression().getType().size(), paramsSize));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    funcAddresses.add(funcDeclaration.name(), new Callable.StaticCallable(funcLabel));

                    block.add(skipLabel);
                }
                // nothing to do for type declarations
                case Declaration.TypeDeclaration _ -> { }
                case Declaration.VarDeclaration varDeclaration -> {
                    block.add(new Instruction.PUSH(varDeclaration.runtimeType().size()));
                    localVars.add(varDeclaration.name(), stackOffset);
                    stackOffset += varDeclaration.runtimeType().size();
                }
                case Declaration.ProcDeclaration procDeclaration -> {
                    //  JUMP skipLabel
                    // procLabel:
                    //  [procBody]
                    //  RETURN 0 paramsSize
                    // skipLabel:

                    Instruction.LABEL skipLabel = labelSupplier.get();
                    Instruction.LABEL procLabel = labelSupplier.get();

                    block.add(new Instruction.JUMP_LABEL(skipLabel));
                    block.add(procLabel);

                    // new scope for local vars and functions
                    localVars.enterNewScope(0);
                    funcAddresses.enterNewScope(null);

                    // the proc currently being declared must be visible in its own definition, so as to allow recursion
                    funcAddresses.add(procDeclaration.name(), new Callable.StaticCallable(procLabel));

                    // we need the total amount of space that will be taken by the parameters to know how to RETURN
                    int paramsSize = addParametersToScope(procDeclaration.parameters());

                    block.addAll(generate(procDeclaration.statement()));
                    block.add(new Instruction.RETURN(0, paramsSize));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    funcAddresses.add(procDeclaration.name(), new Callable.StaticCallable(procLabel));

                    block.add(skipLabel);
                }
            }
        }

        return stackOffset;
    }

    // associates each parameter with its location relative to current frame, returns no words that will be  allocated for
    // parameter when the corresponding function is called
    private int addParametersToScope(List<Parameter> parameters) {
        int paramOffset = -1;
        for (Parameter parameter : parameters.reversed()) {
            switch (parameter) {
                // static link and code address to be on stack
                case Parameter.FuncParameter funcParameter -> funcAddresses.add(
                        funcParameter.getName(),
                        new Callable.DynamicCallable(paramOffset)
                );
                case Parameter.ValueParameter valueParameter -> localVars.add(
                        valueParameter.getName(), paramOffset);
                case VarParameter varParameter -> localVars.add(varParameter.getName(), paramOffset);
            }
            paramOffset -= parameter.getType().size();
        }

        // |paramOffset| - 1, because paramOffset starts at -1
        return (paramOffset * -1) - 1;
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

                if (basicIdentifier.getType() instanceof RuntimeType.RefOf) {
                    block.addAll(generateRuntimeLocation(identifier, true));
                    block.add(new Instruction.STOREI(size));
                } else {
                    block.add(new Instruction.STORE(size, address));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> throw new RuntimeException();
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
                        ((RuntimeType.ArrayType) arraySubscript.array().getType().baseType()).elementType().size()));
                // CALL Primitive.MULT, to get offset
                block.add(new Instruction.CALL_PRIM(Primitive.MULT));
                // CALL Primitive.ADD, to add offset to address of root
                block.add(new Instruction.CALL_PRIM(Primitive.ADD));
                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                Instruction.Address address = lookupAddress(basicIdentifier);
                block.add(new Instruction.LOADA(address));

                // if its a identifier is already a reference, then "dereference" it
                if (dereferencing && basicIdentifier.getType() instanceof RuntimeType.RefOf) {
                    block.add(new Instruction.LOADI(Machine.addressSize));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> throw new RuntimeException();
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

                if (basicIdentifier.getType() instanceof RuntimeType.RefOf) {
                    block.addAll(generateRuntimeLocation(identifier, true));
                    block.add(new Instruction.LOADI(size));
                } else {
                    block.add(new Instruction.LOAD(size, address));
                }

                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> throw new RuntimeException();
        }
    }

    // given a basic identifier, gets the address it may be found in at runtime
    private Instruction.Address lookupAddress(Expression.Identifier.BasicIdentifier basicIdentifier) {
        SymbolTable<Integer, Integer>.DepthLookup lookup = localVars.lookupWithDepth(basicIdentifier.name());
        return new Instruction.Address(getDisplayRegister(lookup.depth()), lookup.t());
    }

    // represents things that may be the target of CALL/CALLI instructions
    sealed interface Callable permits Callable.DynamicCallable, Callable.PrimitiveCallable, Callable.StaticCallable {

        // a callable whose location is known statically
        record StaticCallable(Instruction.LABEL label) implements Callable { }

        // a callable whose closure may be found at the given stackOffset with static nesting depth decided elsewhere
        record DynamicCallable(int stackOffset) implements Callable { }

        // a primitive call
        record PrimitiveCallable(Primitive primitive) implements Callable { }

    }

}
