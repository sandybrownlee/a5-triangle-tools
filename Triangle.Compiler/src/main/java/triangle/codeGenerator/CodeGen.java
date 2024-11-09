package triangle.codeGenerator;

import triangle.abstractMachine.Register;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Statement;
import triangle.contextualAnalyzer.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CodeGen {

    // two types of symbolic references:
    //      code address - for func params, func decls, if-statement branches, while-statement branches
    //      data address - reference into stack

    private final SymbolTable<Instruction.LABEL, Void> funcAddresses = new SymbolTable<>(null);
    // localAddresses needs to store (depth,offset) and keep track of current offset since scopes of localAddresses do not
    //      correspond exactly with order of our function calls
    private final SymbolTable<Integer, Integer>        localVars     = new SymbolTable<>(0);
    private final Supplier<Instruction.LABEL>          labelSupplier = new Supplier<>() {
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
                block.add(new Instruction.POP(0, expressionStatement.expression().getType().size()));
                return block;
            }
            case Statement.AssignStatement assignStatement -> {
                block.addAll(generate(assignStatement.expression()));

                Address varAddress = resolveLocalVar(assignStatement.identifier());
                int varSize = assignStatement.expression().getType().size();

                block.add(new Instruction.STORE(varSize, varAddress));
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
                            SymbolTable<Instruction.LABEL, Void>.DepthLookup lookup = funcAddresses.lookupWithDepth(funcArgument.func().name());
                            // TODO: is this right?
                            block.add(new Instruction.LOADA(new Address(getDisplayRegister(lookup.depth()), 0)));
                            block.add(new Instruction.LOADA_LABEL(lookup.t()));
                        }
                        // load address of var argument
                        case Argument.VarArgument varArgument -> block.add(new Instruction.LOADA(resolveLocalVar(varArgument.var())));
                        // just evaluate expr and leave it on stack
                        case Expression expressionArg -> generate(expressionArg);
                    }
                }

                SymbolTable<Instruction.LABEL, Void>.DepthLookup lookup = funcAddresses.lookupWithDepth(funCall.func().name());
                // TODO: verify static links work as expected
                block.add(new Instruction.CALL(getDisplayRegister(lookup.depth()), lookup.t()));
                return block;
            }
            case Expression.Identifier identifier -> {
                Address varAddress = resolveLocalVar(identifier);
                block.add(new Instruction.LOAD(identifier.getType().size(), varAddress));
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
            case Expression.LitArray litArray -> throw new RuntimeException();
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
                    localVars.add(constDeclaration.name(), stackOffset);
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
                    int paramOffset = 0;
                    for (Parameter parameter : funcDeclaration.parameters()) {
                        // add param to localAddress
                        // TODO: need to actually handle the different parameter types
                        localVars.add(parameter.getName(), paramOffset);
                        paramOffset += parameter.getType().size();
                    }

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
                    localVars.add(varDeclaration.name(), stackOffset);
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

                    // add each param, offset by the appropriate amount, to local addresses
                    int paramOffset = 0;
                    for (Parameter parameter : procDeclaration.parameters()) {
                        // add param to localAddress
                        localVars.add(parameter.getName(), paramOffset);
                        paramOffset += parameter.getType().size();
                    }

                    // add the function currently being declared to funcAddresses
                    funcAddresses.add(procDeclaration.name(), funcLabel);

                    // generate code for the body of the function in the new scope
                    block.addAll(generate(procDeclaration.statement()));
                    localVars.exitScope();

                    block.add(skipLabel);
                }
            }
        }

        return stackOffset;
    }

    private Address resolveLocalVar(Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> throw new RuntimeException();
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                SymbolTable<Integer, Integer>.DepthLookup lookup = localVars.lookupWithDepth(basicIdentifier.name());
                yield new Address(getDisplayRegister(lookup.depth()), lookup.t());
            }
            case Expression.Identifier.RecordAccess recordAccess -> throw new RuntimeException();
        };
    }

    // a list of instructions that, when evaluated, leaves the local address of the identifier, as the top  element in the stack;
    // leaves the stack otherwise unchanged
    private List<Instruction> fetch(Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> {
                Address arrayBase = resolveLocalVar(arraySubscript.root());
            }
            case Expression.Identifier.BasicIdentifier basicIdentifier -> {
                SymbolTable<Integer, Integer>.DepthLookup lookup = localVars.lookupWithDepth(basicIdentifier.name());
                yield new Address(getDisplayRegister(lookup.depth()), lookup.t());
            }
            case Expression.Identifier.RecordAccess recordAccess -> { }
        };
    }

}
