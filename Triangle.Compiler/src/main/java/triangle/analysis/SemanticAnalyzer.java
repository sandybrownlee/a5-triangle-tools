package triangle.analysis;

import triangle.abstractMachine.Machine;
import triangle.repr.Argument;
import triangle.repr.Declaration;
import triangle.repr.Declaration.ConstDeclaration;
import triangle.repr.Declaration.TypeDeclaration;
import triangle.repr.Expression;
import triangle.repr.Expression.*;
import triangle.repr.Expression.Identifier.BasicIdentifier;
import triangle.repr.Parameter;
import triangle.repr.SourcePosition;
import triangle.repr.Statement;
import triangle.util.StdEnv;
import triangle.util.SymbolTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

// SemanticAnalyzer assumes:
//  AST is annotated with source positions

// SemanticAnalyzer ensures:
//  all uses of an identifier are in a scope with a corresponding declaration
//  no duplicate declarations are attempted
//  no duplicate parameters are defined, for each function/procedure
//  record literals are canonicalized
//  no duplicate fields in record literals
//  func/proc parameters are only ever supplied with arguments marked func/proc
//  var parameters are only ever supplied with arguments marked var

// TODO: need to ensure static-nesting depth does not exceed maximum
// TODO: TAM Specification requires second operand of // (mod) must be positive; this needs to be done at runtime
// TODO: to handle ++, we need some kind of rewriting system

// WARNING: this class uses exception as control flow; this is to allow checking to resume from a known "safe point"
public final class SemanticAnalyzer {

    private static void checkArgumentKinds(final List<Argument> arguments, final List<Parameter> declaredParams)
    throws SemanticException {
        for (int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            Parameter param = declaredParams.get(i);

            // if the corresponding parameter was declared var, the argument must be too
            if (param instanceof Parameter.VarParameter && !(arg instanceof Argument.VarArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePosition(), arg, Argument.VarArgument.class);
            }

            // if the corresponding parameter was declared func, the argument must be too
            if (param instanceof Parameter.FuncParameter && !(arg instanceof Argument.FuncArgument)) {
                throw new SemanticException.InvalidArgument(arg.sourcePosition(), arg, Argument.FuncArgument.class);
            }

            if (param instanceof Parameter.ValueParameter && !(arg instanceof Expression)) {
                throw new SemanticException.InvalidArgument(arg.sourcePosition(), arg, Expression.class);
            }
        }
    }

    private final SymbolTable<Binding, Void> terms       = new SymbolTable<>(null);
    private final List<SemanticException>    errors      = new ArrayList<>();
    private final TypeChecker                typeChecker = new TypeChecker();

    {
        // populate initial scope with terms from stdenv
        StdEnv.STD_TERMS.forEach((term, _) -> terms.add(term, new Binding(false, null)));
    }

    public List<SemanticException> analyzeProgram(Statement program) {
        analyze(program);

        if (!this.errors.isEmpty()) {
            System.err.println("Semantic analysis found errors, not proceeding with type checking");
            return errors;
        }

        typeChecker.checkAndAnnotate(program);

        errors.addAll(typeChecker.getErrors());
        return errors;
    }

    private void analyze(final Statement statement) {
        switch (statement) {
            case Statement.ExpressionStatement expressionStatement -> {
                try {
                    analyze(expressionStatement.expression());
                } catch (SemanticException e) {
                    errors.add(e);
                }
            }
            case Statement.AssignStatement assignStatement -> {
                Identifier lvalue = assignStatement.identifier();
                Expression rvalue = assignStatement.expression();

                try {
                    if (lookup(lvalue.root()).constant()) {
                        errors.add(new SemanticException.AssignmentToConstant(assignStatement.sourcePosition(), lvalue));
                    }

                    analyze(rvalue);
                    analyze(lvalue);
                } catch (SemanticException e) {
                    errors.add(e);
                }
            }
            case Statement.IfStatement ifStatement -> {
                Expression condition = ifStatement.condition();
                Optional<Statement> consequent = ifStatement.consequent();
                Optional<Statement> alternative = ifStatement.alternative();

                try {
                    analyze(condition);
                } catch (SemanticException e) {
                    errors.add(e);
                }

                consequent.ifPresent(this::analyze);
                alternative.ifPresent(this::analyze);
            }
            case Statement.LetStatement letStatement -> {
                List<Declaration> declarations = letStatement.declarations();
                Statement stmt = letStatement.statement();

                try {
                    terms.enterNewScope(null);
                    bindDeclarations(declarations);
                    // analyze the statement in the new environment
                    analyze(stmt);
                    terms.exitScope();
                } catch (SemanticException e) {
                    errors.add(e);
                }
            }
            case Statement.LoopWhileStatement loopWhileStatement -> {
                visitLoop(loopWhileStatement.condition(), loopWhileStatement.loopBody());
                analyze(loopWhileStatement.doBody());
            }
            case Statement.RepeatUntilStatement repeatUntilStatement -> visitLoop(
                    repeatUntilStatement.condition(), repeatUntilStatement.body());
            case Statement.RepeatWhileStatement repeatWhileStatement -> visitLoop(
                    repeatWhileStatement.condition(), repeatWhileStatement.body());
            case Statement.StatementBlock statementBlock -> {
                for (Statement stmt : statementBlock.statements()) {
                    analyze(stmt);
                }
            }
            case Statement.WhileStatement whileStatement -> visitLoop(whileStatement.condition(), whileStatement.body());
        }
    }

    // analyze(Expression) needs to throw SemanticException; since an expression may be part of a larger declaration and it
    // wont know where to rewind to
    private void analyze(final Expression expression) throws SemanticException {
        switch (expression) {
            case BinaryOp binop -> {
                analyze(binop.leftOperand());
                analyze(binop.rightOperand());
            }
            case FunCall funCall -> {
                Identifier callable = funCall.func();
                List<Argument> arguments = funCall.arguments();

                analyze(callable);

                Declaration declaration = lookup(funCall.func()).declaration();
                // if there is a corresponding declaration for this function (i.e., it is not a primitive call) check if the
                //  arguments being passed correspond to the parameters; TAM specification requires that a parameter declared var
                //  must have a corresponding argument declared var; etc.
                if (declaration instanceof Declaration.ProcDeclaration procDeclaration) {
                    checkArgumentKinds(arguments, procDeclaration.parameters());
                } else if (declaration instanceof Declaration.FuncDeclaration funcDeclaration) {
                    checkArgumentKinds(arguments, funcDeclaration.parameters());
                }

                for (Argument arg : funCall.arguments()) {
                    analyze(arg);
                }
            }
            case Identifier identifier -> analyze(identifier);
            case IfExpression ifExpression -> {
                analyze(ifExpression.condition());
                analyze(ifExpression.consequent());
                analyze(ifExpression.alternative());
            }
            case LetExpression letExpression -> {
                List<Declaration> declarations = letExpression.declarations();
                Expression expr = letExpression.expression();

                terms.enterNewScope(null);
                bindDeclarations(declarations);
                analyze(expr);
                terms.exitScope();
            }
            case LitArray litArray -> {
                for (Expression value : litArray.elements()) {
                    analyze(value);
                }
            }
            case LitChar _ -> { }
            case LitInt litInt -> {
                SourcePosition sourcePos = litInt.sourcePosition();
                int value = litInt.value();
                // the TAM specification defines `Integer` to be between -maxint...+maxint (not two's complement!)
                if (value > Machine.maxintRep || value < (Machine.maxintRep * -1)) {
                    throw new SemanticException.IntegerLiteralTooLarge(sourcePos, value);
                }
            }
            case LitRecord litRecord -> {
                // canonicalize fields
                litRecord.fields().sort(Comparator.comparing(LitRecord.RecordField::name));

                Set<String> seenFieldNames = new HashSet<>();
                for (LitRecord.RecordField field : litRecord.fields()) {
                    if (seenFieldNames.contains(field.name())) {
                        throw new SemanticException.DuplicateRecordField(litRecord.sourcePosition(), field);
                    }
                    seenFieldNames.add(field.name());
                    analyze(field.value());
                }
            }
            case UnaryOp unaryOp -> {
                lookup(unaryOp.operator());
                analyze(unaryOp.operand());
            }
            case LitBool _ -> { }
            case Expression.SequenceExpression sequenceExpression -> {
                analyze(sequenceExpression.statement());
                analyze(sequenceExpression.expression());
            }
        }
    }

    private void analyze(final Identifier identifier) throws SemanticException {
        switch (identifier) {
            case Identifier.ArraySubscript arraySubscript -> {
                analyze(arraySubscript.array());
                analyze(arraySubscript.subscript());
            }
            case BasicIdentifier basicIdentifier -> lookup(basicIdentifier);
            // NOTE: record's field must be semantically analyzed in the type checker, since field names are only available
            //  after type-resolution of the record, see TypeChecker.checkAndAnnotate(Identifier)
            case Identifier.RecordAccess recordAccess -> analyze(recordAccess.record());
        }
    }

    private void analyze(final Argument argument) throws SemanticException {
        switch (argument) {
            case Argument.FuncArgument funcArgument -> analyze(funcArgument.func());
            case Argument.VarArgument varArgument -> {
                analyze(varArgument.var());
                // not allowed to pass a const as a var argument; in principle we could analyze the called procedure to see if
                // it mutates the corresponding var argument, but it is quite complicated for little benefit; we assume that if
                // a function marks a parameter as a var that it intends to mutate it
                if (lookup(varArgument.var().root()).constant()) {
                    throw new SemanticException.AssignmentToConstant(varArgument.sourcePosition(), varArgument.var().root());
                }
            }
            case Expression expression -> analyze(expression);
        }
    }

    // analyze(Declaration) needs to throw SemanticException instead of merely adding errors to the list, because it does not
    // know to what point to "rewind" to to continue analysis
    private void bindDeclarations(List<Declaration> declarations) throws SemanticException {
        Set<Declaration> seenDeclarations = new HashSet<>();
        for (Declaration declaration : declarations) {
            if (seenDeclarations.contains(declaration)) {
                throw new SemanticException.DuplicateDeclaration(declaration.sourcePosition(), declaration);
            }

            switch (declaration) {
                case ConstDeclaration constDeclaration -> {
                    try {
                        analyze(constDeclaration.value());
                        terms.add(constDeclaration.name(), new Binding(true, constDeclaration));
                    } catch (SemanticException.DuplicateRecordTypeField e) {
                        // rethrow duplicate record fields with added source position info
                        throw new SemanticException.DuplicateRecordTypeField(constDeclaration.sourcePosition(), e.getFieldType());
                    }
                }
                case Declaration.FuncDeclaration funcDeclaration -> {
                    // function must be bound in its own definition, to allow recursion
                    terms.add(funcDeclaration.name(), new Binding(false, funcDeclaration));

                    try {
                        terms.enterNewScope(null);
                        bindParameters(funcDeclaration.parameters());
                        analyze(funcDeclaration.expression());
                    } catch (SemanticException.DuplicateRecordTypeField e) {
                        // rethrow duplicate record fields with added source position info
                        throw new SemanticException.DuplicateRecordTypeField(funcDeclaration.sourcePosition(), e.getFieldType());
                    } catch (SemanticException e) {
                        errors.add(e);
                        // we can continue binding other declarations
                    } finally {
                        // treat this function as being bound
                        terms.exitScope();
                    }
                }
                case TypeDeclaration _ -> { }
                case Declaration.VarDeclaration varDeclaration -> terms.add(
                        varDeclaration.name(), new Binding(false, varDeclaration));
                case Declaration.ProcDeclaration procDeclaration -> {
                    // proc must be visible in its own definition
                    terms.add(procDeclaration.name(), new Binding(false, procDeclaration));

                    try {
                        terms.enterNewScope(null);
                        bindParameters(procDeclaration.parameters());
                        analyze(procDeclaration.statement());
                    } catch (SemanticException e) {
                        errors.add(e);
                        // continue processing other declarations
                    } finally {
                        // treat this proc as being bound
                        terms.exitScope();
                    }
                }
            }

            seenDeclarations.add(declaration);
        }
    }

    private void bindParameters(final List<Parameter> parameters) throws SemanticException.DuplicateParameter {
        Set<String> seenParameters = new HashSet<>();
        for (Parameter parameter : parameters) {
            if (seenParameters.contains(parameter.name())) {
                throw new SemanticException.DuplicateParameter(parameter.sourcePosition(), parameter);
            }

            seenParameters.add(parameter.name());
            terms.add(parameter.name(), new Binding(false, null));
        }
    }

    private Binding lookup(final BasicIdentifier basicIdentifier) throws SemanticException {
        try {
            return terms.lookup(basicIdentifier.name());
        } catch (NoSuchElementException e) {
            throw new SemanticException.UndeclaredUse(basicIdentifier.sourcePosition(), basicIdentifier);
        }
    }

    private void visitLoop(final Expression condition, final Statement body) {
        try {
            analyze(condition);
        } catch (SemanticException e) {
            errors.add(e);
        }

        analyze(body);
    }

    // each identifier is bound to whether or not its constant, and the place where it was declared
    record Binding(boolean constant, Declaration declaration) { }

}
