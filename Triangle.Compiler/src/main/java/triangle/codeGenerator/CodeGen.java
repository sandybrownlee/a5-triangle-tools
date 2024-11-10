package triangle.codeGenerator;

import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Parameter.VarParameter;
import triangle.ast.Statement;
import triangle.contextualAnalyzer.SymbolTable;
import triangle.types.RuntimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

// TODO: verify if var arguments/parameters are handled correctly; does passing a const as a var break things?
// TODO: verify func/proc arguments/parameters are handled correctly;
// TODO: verify static links work as expected
public class CodeGen {
    private final SymbolTable<Instruction.LABEL, Void> funcAddresses = new SymbolTable<>(null);
    private final SymbolTable<VarState, Integer> localVars     = new SymbolTable<>(0);
    private final Supplier<Instruction.LABEL>    labelSupplier = new Supplier<>() {
        private int i = 0;

        @Override public Instruction.LABEL get() {
            return new Instruction.LABEL(i++);
        }
    };

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

    public List<Instruction> compile(Statement statement) {
        var x = generate(statement);
        x.add(new Instruction.HALT());
        return x;
    }

    private List<Instruction> generate(Statement statement) {
        List<Instruction> block = new ArrayList<>();

        switch (statement) {
            case Statement.ExpressionStatement expressionStatement -> {
                block.addAll(generate(expressionStatement.expression()));
                // discard results of ExpressionStatements, since they are intended to be used only for side-effects
                int resultSize = expressionStatement.expression().getType().size();
                if (resultSize > 0) {
                    block.add(new Instruction.POP(0, resultSize));
                }
                return block;
            }
            case Statement.AssignStatement assignStatement -> {
                VarState lookup = localVars.lookup(assignStatement.identifier().root().name());
                // evaluate expression
                block.addAll(generate(assignStatement.expression()));
                // evaluate address of identifier and put on stack
                block.addAll(evaluateAddress(assignStatement.identifier()));
                // STOREI
                block.add(new Instruction.STOREI(assignStatement.expression().getType().size()));
                return block;
            }
            case Statement.IfStatement ifStatement -> {
                block.addAll(generate(ifStatement.condition()));
                Instruction.LABEL skipLabel = labelSupplier.get();
                block.add(new Instruction.JUMPIF(0, skipLabel));

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
            case Statement.LoopWhileStatement loopWhileStatement -> throw new RuntimeException();
            case Statement.RepeatUntilStatement repeatUntilStatement -> throw new RuntimeException();
            case Statement.RepeatWhileStatement repeatWhileStatement -> throw new RuntimeException();
            case Statement.StatementBlock statementBlock -> {
                for (Statement aStatement : statementBlock.statements()) {
                    block.addAll(generate(aStatement));
                }

                return block;
            }
            case Statement.WhileStatement whileStatement -> throw new RuntimeException();
        }
    }

    private List<Instruction> generate(Expression expression) {
        List<Instruction> block = new ArrayList<>();

        switch (expression) {
            case Expression.BinaryOp binaryOp -> throw new RuntimeException();
            case Expression.FunCall funCall -> {
                for (Argument argument : funCall.arguments()) {
                    switch (argument) {
                        // put a closure - static link + code address - onto the stack
                        case Argument.FuncArgument funcArgument -> {
                            SymbolTable<Instruction.LABEL, Void>.DepthLookup lookup =
                                    funcAddresses.lookupWithDepth(funcArgument.func().name());
                            block.add(new Instruction.LOADA(new Address(getDisplayRegister(lookup.depth()), 0)));
                            block.add(new Instruction.LOADA_LABEL(lookup.t()));
                        }
                        // load address of var argument
                        case Argument.VarArgument varArgument -> block.addAll(evaluateAddress(varArgument.var()));
                        // just evaluate expr and leave it on stack
                        case Expression expressionArg -> block.addAll(generate(expressionArg));
                    }
                }

                try {
                    SymbolTable<VarState, Integer>.DepthLookup lookup =  localVars.lookupWithDepth(funCall.func().name());
                    if (lookup.t().isFunc()) {
                        // the frame and offset that the local local func is found in
                        Register thisFrame = getDisplayRegister(lookup.depth());
                        int closureStackOffset = lookup.t().stackOffset();

                        // TODO: make sure static-link and code address are loaded in the correct order
                        // static link is a 1-word value ...
                        block.add(new Instruction.LOAD(1, new Address(thisFrame, closureStackOffset)));
                        // ... immediately followed by the code address
                        block.add(new Instruction.LOAD(1, new Address(thisFrame, closureStackOffset + 1)));
                        // then just call the closure
                        block.add(new Instruction.CALLI());
                        return block;
                    }
                    // false alarm; this means we just have a local parameter with the same name as the function we want to
                    // call
                } catch (NoSuchElementException _) {
                }
                // if we are here, this means that the function must be found statically in our lexical scope
                SymbolTable<Instruction.LABEL, Void>.DepthLookup funcLabel = funcAddresses.lookupWithDepth(funCall.func().name());
                // then just make a standard call; we can figure out the static link just by the nesting depth of the
                //  funcAddress lookup
                block.add(new Instruction.CALL(getDisplayRegister(funcLabel.depth()), funcLabel.t()));
                return block;
            }
            case Expression.Identifier identifier -> {
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
                // just generate all the values and leave them on the stack contigously
                for (Expression e : litArray.elements()) {
                    block.addAll(generate(e));
                }
                return block;
            }
            case Expression.LitBool litBool -> throw new RuntimeException();
            case Expression.LitChar litChar -> throw new RuntimeException();
            case Expression.LitInt litInt -> {
                block.add(new Instruction.LOADL(litInt.value()));
                return block;
            }
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
                    block.add(new Instruction.JUMP(skipLabel));

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
                    block.add(new Instruction.JUMP(skipLabel));

                    // LABEL funcLabel
                    block.add(funcLabel);

                    // create a new local address scope where the parameters are going to be visible
                    localVars.enterNewScope(0);
                    funcAddresses.enterNewScope(null);

                    // add each param, offset by the appropriate amount, to local addresses
                    int paramOffset = allocateParameters(procDeclaration.parameters());

                    // add the function currently being declared to funcAddresses
                    funcAddresses.add(procDeclaration.name(), funcLabel);

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
                case Parameter.FuncParameter funcParameter ->
                        localVars.add(funcParameter.getName(), new VarState(paramOffset, false, true));
                case Parameter.ValueParameter valueParameter ->
                        localVars.add(valueParameter.getName(), new VarState(paramOffset, false, false));
                case VarParameter varParameter ->
                        localVars.add(varParameter.getName(), new VarState(paramOffset, true, false));
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
                block.add(new Instruction.LOADA(new Address(getDisplayRegister(lookup.depth()), lookup.t().stackOffset)));
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
