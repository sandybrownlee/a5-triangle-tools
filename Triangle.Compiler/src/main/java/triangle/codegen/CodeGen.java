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
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: verify if var arguments/parameters are handled correctly; does passing a const as a var break things?
// TODO: verify func/proc arguments/parameters are handled correctly;
// TODO: verify static links work as expected
// TODO: variable access is kinda hacky rn; figure out a way to deal with var args, func args, and plain values seamlessly
public class CodeGen {

    // maps primitives to their number
    private static final Map<String, Primitive> primitives = new HashMap<>();

    static {
        // TODO: fill up
        primitives.put(">", Primitive.GT);
        primitives.put(">=", Primitive.GE);
        primitives.put("-", Primitive.SUB);
        primitives.put("putint", Primitive.PUTINT);
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
            default -> throw new RuntimeException("static-nesting depth exceeded");
        };
    }

    private static void write(final List<Instruction> instructions) {
        try (DataOutputStream fw = new DataOutputStream(new FileOutputStream("output.tam"))) {
            for (Instruction instruction : instructions) {
                // TODO: this is too ugly to stay + fill in
                switch (instruction) {
                    case Instruction.CALL call -> {
                        fw.writeInt(6);
                        fw.writeInt(call.address().r().ordinal());
                        fw.writeInt(call.staticLink().ordinal());
                        fw.writeInt(call.address().d());
                    }
                    case Instruction.CALLI calli -> { }
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
                    case Instruction.LOAD load -> { }
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
                    case Instruction.RETURN aReturn -> { }
                    case Instruction.STORE store -> { }
                    case Instruction.STOREI storei -> {
                        fw.writeInt(5);
                        fw.writeInt(0);
                        fw.writeInt(storei.size());
                        fw.writeInt(0);
                    }
                    case Instruction.CALL_LABEL callLabel -> throw new RuntimeException("write of pseudo-instruction");
                    case Instruction.CALL_PRIM callPrim -> throw new RuntimeException("write of pseudo-instruction");
                    case Instruction.JUMPIF_LABEL jumpifLabel -> throw new RuntimeException("write of pseudo-instruction");
                    case Instruction.JUMP_LABEL jumpLabel -> throw new RuntimeException("write of pseudo-instruction");
                    case Instruction.LABEL label -> throw new RuntimeException("write of pseudo-instruction");
                    case Instruction.LOADA_LABEL loadaLabel -> throw new RuntimeException("write of pseudo-instruction");
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
        Function<Instruction.LABEL, Instruction.Address> toCodeAddress = label -> new Instruction.Address(Register.CB,
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
    private final SymbolTable<Instruction.LABEL, Void> funcAddresses = new SymbolTable<>(null);
    private final SymbolTable<VarState, Integer>       localVars     = new SymbolTable<>(0);
    private final Supplier<Instruction.LABEL>          labelSupplier = new Supplier<>() {
        private int i = 0;

        @Override public Instruction.LABEL get() {
            return new Instruction.LABEL(i++);
        }
    };

    public List<Instruction> compile(Statement statement) {
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

    // the non-trivial code-generation bits below have comments explaining the structure of the generated code:
    //      [xyz] denotes the result of generating code for xyz
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
                block.addAll(evaluateAddress(assignStatement.identifier()));
                // STOREI
                block.add(new Instruction.STOREI(assignStatement.expression().getType().size()));
                return block;
            }
            case Statement.IfStatement ifStatement -> {
                Instruction.LABEL skipLabel = labelSupplier.get();

                //  [condition]
                //  JUMPIF 0 skipLabel
                //  [consequent]
                // skipLabel:
                //  [alternative]

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

                // as declarations are evaluated, they will expand the stack; new consts/vars are stored at stack[stackOffset]
                int savedStackOffset = localVars.scopeLocalState();
                int newStackOffset = allocateDeclarations(block, letStatement.declarations());

                localVars.setScopeLocalState(newStackOffset);
                // letStatement.statement() is generated in the new scope containing all our definitions
                block.addAll(generate(letStatement.statement()));

                if (newStackOffset - savedStackOffset > 0) {
                    block.add(new Instruction.POP(0, newStackOffset - savedStackOffset));
                }

                return block;
            }
            case Statement.LoopWhileStatement loopWhileStatement -> {
                Instruction.LABEL loopLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                // loopLabel:
                //  [loopBody]
                //  [condition]
                //  JUMPIF 0 skipLabel
                //  [doBody]
                //  JUMP loopLabel
                // skipLabel:

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
                Instruction.LABEL loopLabel = labelSupplier.get();
                Instruction.LABEL skipLabel = labelSupplier.get();

                // loopLabel:
                //  [condition]
                //  JUMPIF 0 skipLabel
                //  [body]
                //  JUMP loopLabel
                // skipLabel:

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
                // evaluate left operand and leave on stack
                block.addAll(generate(binaryOp.leftOperand()));
                // evaluate right operand and leave on stack
                block.addAll(generate(binaryOp.rightOperand()));
                // TODO: don't hardcode primitives
                block.add(new Instruction.CALL_PRIM(primitives.get(binaryOp.operator().name())));
                return block;
            }
            case Expression.FunCall funCall -> {

                //  [arguments(1)]
                //  [arguments(2)]
                //  ...
                //  [arguments(n)]

                for (Argument argument : funCall.arguments()) {
                    switch (argument) {
                        // put a closure - static link + code address - onto the stack
                        case Argument.FuncArgument funcArgument -> {
                            SymbolTable<Instruction.LABEL, Void>.DepthLookup lookup =
                                    funcAddresses.lookupWithDepth(funcArgument.func().name());
                            block.add(new Instruction.LOADA(new Instruction.Address(getDisplayRegister(lookup.depth()), 0)));
                            block.add(new Instruction.LOADA_LABEL(lookup.t()));
                        }
                        // load address of var argument
                        case Argument.VarArgument varArgument -> block.addAll(evaluateAddress(varArgument.var()));
                        // just evaluate expr and leave it on stack
                        case Expression expressionArg -> block.addAll(generate(expressionArg));
                    }
                }

                try {
                    SymbolTable<VarState, Integer>.DepthLookup lookup = localVars.lookupWithDepth(funCall.func().name());
                    if (lookup.t().isFunc()) {
                        // TODO: make sure static-link and code address are loaded in the correct order
                        // the frame and offset that the local local func is found in
                        Register staticLink = getDisplayRegister(lookup.depth());
                        int closureStackOffset = lookup.t().stackOffset();

                        //  LOAD 1 (closureStackOffset) [staticLink]
                        //  LOAD 1 (closureStackOffset + 1) [staticLink]
                        //  CALLI

                        // static link is a 1-word value ...
                        block.add(new Instruction.LOAD(1, new Instruction.Address(staticLink, closureStackOffset)));
                        // ... immediately followed by the code address
                        block.add(new Instruction.LOAD(1, new Instruction.Address(staticLink, closureStackOffset + 1)));
                        // then just call the closure
                        block.add(new Instruction.CALLI());
                        return block;
                    }
                    // this means we just have a local parameter with the same name as the function we want to call
                } catch (NoSuchElementException _) { }

                if (primitives.containsKey(funCall.func().name())) {
                    block.add(new Instruction.CALL_PRIM(primitives.get(funCall.func().name())));
                    return block;
                }

                // if we are here, this means that the function must be found statically in our lexical scope
                SymbolTable<Instruction.LABEL, Void>.DepthLookup funcLabel = funcAddresses.lookupWithDepth(funCall.func().name());
                // then just make a standard call; we can figure out the static link just by the nesting depth of the
                //  funcAddress lookup
                block.add(new Instruction.CALL_LABEL(getDisplayRegister(funcLabel.depth()), funcLabel.t()));
                return block;
            }
            case Expression.Identifier identifier -> {

                //  evaluateAddress(identifier)
                //  LOADI identifierSize

                block.addAll(evaluateAddress(identifier));
                block.add(new Instruction.LOADI(identifier.getType().size()));
                return block;
            }
            case Expression.IfExpression ifExpression -> throw new RuntimeException();
            case Expression.LetExpression letExpression -> {
                // as declarations are evaluated, they will expand the stack; new consts/vars are stored at stack[stackOffset]
                int savedStackOffset = localVars.scopeLocalState();
                int newStackOffset = allocateDeclarations(block, letExpression.declarations());

                // letExpression.statement() is generated in the new scope containing all our definitions
                block.addAll(generate(letExpression.expression()));

                int resultSize = letExpression.expression().getType().size();
                int localVarsSize = newStackOffset - savedStackOffset;
                if (localVarsSize > 0) {
                    block.add(new Instruction.POP(resultSize, localVarsSize));
                }

                return block;
            }
            case Expression.LitArray litArray -> {
                // just generate all the values and leave them on the stack contiguously
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

    private int allocateDeclarations(final List<Instruction> block, final List<Declaration> declarations) {
        int stackOffset = localVars.scopeLocalState();

        for (Declaration declaration : declarations) {
            switch (declaration) {
                case Declaration.ConstDeclaration constDeclaration -> {
                    block.addAll(generate(constDeclaration.value()));
                    localVars.add(constDeclaration.name(), new VarState(stackOffset));
                    stackOffset += constDeclaration.value().getType().size();
                }
                case Declaration.FuncDeclaration funcDeclaration -> {
                    // skipLabel points to a label to skip to so as to not execute the function body while declaring it
                    Instruction.LABEL skipLabel = labelSupplier.get();
                    // funcLabel points to a label, stored in funcAddresses, that other parts of the code can refer to to
                    //      call the function currently being declared
                    Instruction.LABEL funcLabel = labelSupplier.get();

                    // JUMP skipLabel
                    block.add(new Instruction.JUMP_LABEL(skipLabel));

                    // LABEL funcLabel
                    block.add(funcLabel);

                    // add the function currently being declared to funcAddresses
                    funcAddresses.add(funcDeclaration.name(), funcLabel);

                    // create a new local address scope where the parameters are going to be visible
                    localVars.enterNewScope(0);
                    funcAddresses.enterNewScope(null);

                    // add each param, offset by the appropriate amount, to local addresses
                    int paramOffset = allocateParameters(funcDeclaration.parameters());

                    // the function currently being declared must be visible in its own definition, to allow recursion
                    funcAddresses.add(funcDeclaration.name(), funcLabel);

                    // generate code for the body of the function in the new scope
                    // we want to explicitly cast it to an expression, if its type is not Statement, so as to not discard
                    //  the return value
                    block.addAll(generate(funcDeclaration.expression()));
                    block.add(new Instruction.RETURN(funcDeclaration.expression().getType().size(), paramOffset));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    block.add(skipLabel);
                }
                // nothing to do for type declarations
                case Declaration.TypeDeclaration _ -> { }
                case Declaration.VarDeclaration varDeclaration -> {
                    block.add(new Instruction.PUSH(varDeclaration.runtimeType().size()));
                    localVars.add(varDeclaration.name(), new VarState(stackOffset));
                    stackOffset += varDeclaration.runtimeType().size();
                }
                case Declaration.ProcDeclaration procDeclaration -> {
                    // skipLabel points to a label to skip to so as to not execute the function body while declaring it
                    Instruction.LABEL skipLabel = labelSupplier.get();
                    // funcLabel points to a label, stored in funcAddresses, that other parts of the code can refer to to
                    //      call the function currently being declared
                    Instruction.LABEL funcLabel = labelSupplier.get();

                    // JUMP skipLabel
                    block.add(new Instruction.JUMP_LABEL(skipLabel));

                    // LABEL funcLabel
                    block.add(funcLabel);

                    // add the function currently being declared to funcAddresses
                    funcAddresses.add(procDeclaration.name(), funcLabel);

                    // create a new local address scope where the parameters are going to be visible
                    localVars.enterNewScope(0);
                    funcAddresses.enterNewScope(null);

                    // add each param, offset by the appropriate amount, to local addresses
                    int paramOffset = allocateParameters(procDeclaration.parameters());

                    // generate code for the body of the function in the new scope
                    block.addAll(generate(procDeclaration.statement()));

                    // a proc has no return value
                    block.add(new Instruction.RETURN(0, paramOffset));

                    localVars.exitScope();
                    funcAddresses.exitScope();

                    block.add(skipLabel);
                }
            }
        }

        return stackOffset;
    }

    // associates each parameter with its location relative to current frame, returns total words allocated for parameter
    private int allocateParameters(List<Parameter> parameters) {
        int paramOffset = -1;
        for (Parameter parameter : parameters.reversed()) {
            switch (parameter) {
                // static link and code address to be on stack
                case Parameter.FuncParameter funcParameter -> localVars.add(
                        funcParameter.getName(), new VarState(paramOffset, false, true));
                case Parameter.ValueParameter valueParameter -> localVars.add(
                        valueParameter.getName(), new VarState(paramOffset, false, false));
                case VarParameter varParameter -> localVars.add(varParameter.getName(), new VarState(paramOffset, true, false));
            }
            paramOffset -= parameter.getType().size();
        }

        // the total size allocated for parameters is the abs value of paramOffset, minus one because paramOffset starts at -1
        return (paramOffset * -1) - 1;
    }

    // a list of instructions that, when evaluated, leaves the address of the identifier, as the top  element in the stack
    // leaves the stack otherwise unchanged
    private List<Instruction> evaluateAddress(Expression.Identifier identifier) {
        List<Instruction> block = new ArrayList<>();
        switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                // push address of array base on stack
                block.addAll(evaluateAddress(arraySubscript.array()));
                // evaluate subscript and leaves its result on stack
                block.addAll(generate(arraySubscript.subscript()));
                // push array element type size on stack
                int elemSize = ((RuntimeType.ArrayType) arraySubscript.array().getType()).elementType().size();
                block.add(new Instruction.LOADL(elemSize));
                // multiply top two, to get offset
                block.add(new Instruction.CALL_PRIM(Primitive.MULT));
                // add top two, to get final address
                block.add(new Instruction.CALL_PRIM(Primitive.ADD));
                return block;
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                SymbolTable<VarState, Integer>.DepthLookup lookup = localVars.lookupWithDepth(basicIdentifier.name());
                block.add(new Instruction.LOADA(
                        new Instruction.Address(getDisplayRegister(lookup.depth()), lookup.t().stackOffset)));
                if (lookup.t().isReference()) {
                    // if its a reference then we want to dereference it
                    block.add(new Instruction.LOADI(basicIdentifier.getType().size()));
                }
                return block;
            }
            case Expression.Identifier.RecordAccess recordAccess -> {
                // calculate the offset of the accessed field from the base address of the record
                int offset = 0;
                for (RuntimeType.RecordType.FieldType fieldType : ((RuntimeType.RecordType) recordAccess.record()
                                                                                                        .getType()).fieldTypes()) {
                    if (fieldType.fieldName().equals(recordAccess.field().root().name())) {
                        break;
                    }
                    offset += fieldType.fieldType().size();
                }
                // evaluate base address of record and leave on stack
                block.addAll(evaluateAddress(recordAccess.record()));
                // load offset literal onto stack
                block.add(new Instruction.LOADL(offset));
                // call primitive add
                block.add(new Instruction.CALL_PRIM(Primitive.ADD));
                return block;
            }
        }
    }

    // localAddresses needs to store (depth,offset) and keep track of current offset since scopes of localAddresses do not
    //      correspond with order of our function calls
    record VarState(int stackOffset, boolean isReference, boolean isFunc) {

        // for the common case of adding a plain variable
        VarState(int stackOffset) {
            this(stackOffset, false, false);
        }

    }

}
