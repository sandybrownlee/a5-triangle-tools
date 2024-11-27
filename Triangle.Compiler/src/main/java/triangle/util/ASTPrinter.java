package triangle.util;

import triangle.repr.Argument;
import triangle.repr.Declaration;
import triangle.repr.Expression;
import triangle.repr.Parameter;
import triangle.repr.Statement;
import triangle.repr.TypeSig;

import java.util.List;
import java.util.function.Function;

public final class ASTPrinter {

    public static String prettyPrint(final Statement statement) {
        return switch (statement) {
            case Statement.ExpressionStatement expression -> "{" + prettyPrint(expression.expression()) + "}";
            case Statement.AssignStatement assignStatement -> String.format(
                    "{%s := %s}",
                    prettyPrint(assignStatement.identifier()),
                    prettyPrint(assignStatement.expression())
            );
            case Statement.IfStatement ifStatement -> String.format(
                    "{IF %s THEN %s ELSE %s}",
                    prettyPrint(ifStatement.condition()),
                    ifStatement.consequent().map(ASTPrinter::prettyPrint).orElse(""),
                    ifStatement.alternative().map(ASTPrinter::prettyPrint).orElse("")
            );
            case Statement.LetStatement letStatement -> String.format(
                    "{LET %s IN %s}",
                    makeDelimited(letStatement.declarations(), ASTPrinter::prettyPrint),
                    prettyPrint(letStatement.statement())
            );
            case Statement.LoopWhileStatement loopWhileStatement -> String.format(
                    "{LOOP %s WHILE %s DO %s}",
                    prettyPrint(loopWhileStatement.loopBody()),
                    prettyPrint(loopWhileStatement.condition()),
                    prettyPrint(loopWhileStatement.doBody())
            );
            case Statement.RepeatUntilStatement repeatUntilStatement -> String.format(
                    "{REPEAT %s UNTIL %s}",
                    prettyPrint(repeatUntilStatement.body()),
                    prettyPrint(repeatUntilStatement.condition())
            );
            case Statement.RepeatWhileStatement repeatWhileStatement -> String.format(
                    "{REPEAT %s UNTIL %s}",
                    prettyPrint(repeatWhileStatement.body()),
                    prettyPrint(repeatWhileStatement.condition())
            );
            case Statement.StatementBlock statementBlock -> String.format(
                    "{BEGIN %s END}",
                    makeDelimited(statementBlock.statements(), ASTPrinter::prettyPrint)
            );
            case Statement.WhileStatement whileStatement -> String.format(
                    "{WHILE %s DO %s}",
                    prettyPrint(whileStatement.condition()),
                    prettyPrint(whileStatement.body())
            );
            case Statement.NoopStatement noopStatement -> "NOOP";
        };
    }

    private static String prettyPrint(final Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument -> prettyPrint(funcArgument.func());
            case Argument.VarArgument varArgument -> prettyPrint(varArgument.var());
            case Expression expression -> prettyPrint(expression);
        };
    }

    private static String prettyPrint(final Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration -> String.format(
                    "<CONST %s = %s>",
                    constDeclaration.name(),
                    prettyPrint(constDeclaration.value())
            );
            case Declaration.FuncDeclaration funcDeclaration -> String.format(
                    "<FUNC %s (%s) : %s ~ %s>",
                    funcDeclaration.name(),
                    makeDelimited(funcDeclaration.parameters(), ASTPrinter::prettyPrint),
                    prettyPrint(funcDeclaration.returnTypeSig()),
                    prettyPrint(funcDeclaration.expression())
            );
            case Declaration.TypeDeclaration typeDeclaration -> String.format(
                    "<TYPE %s : %s>",
                    typeDeclaration.name(),
                    prettyPrint(typeDeclaration.typeSig())
            );
            case Declaration.VarDeclaration varDeclaration -> String.format(
                    "<VAR %s : %s>",
                    varDeclaration.name(),
                    prettyPrint(varDeclaration.declaredType())
            );
            case Declaration.ProcDeclaration procDeclaration -> String.format(
                    "<PROC %s (%s) ~ %s>",
                    procDeclaration.name(),
                    makeDelimited(procDeclaration.parameters(), ASTPrinter::prettyPrint),
                    prettyPrint(procDeclaration.statement())
            );
        };
    }

    private static String prettyPrint(final Expression expression) {
        return switch (expression) {
            case Expression.BinaryOp binaryOp -> String.format(
                    "(BINOP %s %s %s)",
                    prettyPrint(binaryOp.operator()),
                    prettyPrint(binaryOp.leftOperand()),
                    prettyPrint(binaryOp.rightOperand())
            );
            case Expression.FunCall funCall -> String.format(
                    "(FUNCALL %s (%s))",
                    prettyPrint(funCall.func()),
                    makeDelimited(funCall.arguments(), ASTPrinter::prettyPrint)
            );
            case Expression.Identifier identifier -> prettyPrint(identifier);
            case Expression.IfExpression ifExpression -> String.format(
                    "(IF %s THEN %s ELSE %s)",
                    prettyPrint(ifExpression.condition()),
                    prettyPrint(ifExpression.consequent()),
                    prettyPrint(ifExpression.alternative())
            );
            case Expression.LetExpression letExpression -> String.format(
                    "(LET %s IN %s)",
                    makeDelimited(letExpression.declarations(), ASTPrinter::prettyPrint),
                    prettyPrint(letExpression.expression())
            );
            case Expression.LitArray litArray -> String.format(
                    "[%s]",
                    makeDelimited(litArray.elements(), ASTPrinter::prettyPrint)
            );
            case Expression.LitChar litChar -> Character.toString(litChar.value());
            case Expression.LitInt litInt -> Integer.toString(litInt.value());
            case Expression.LitRecord litRecord -> String.format(
                    "{%s}",
                    makeDelimited(litRecord.fields(), f -> f.name() + " ~ " + prettyPrint(f.value()))
            );
            case Expression.UnaryOp unaryOp -> String.format(
                    "(UNARYOP %s %s)",
                    prettyPrint(unaryOp.operator()),
                    prettyPrint(unaryOp.operand())
            );
            case Expression.LitBool litBool -> Boolean.toString(litBool.value());
            case Expression.SequenceExpression sequenceExpression -> String.format(
                    "(AFTER %s RETURN %s)",
                    prettyPrint(sequenceExpression.statement()),
                    prettyPrint(sequenceExpression.expression())
            );
        };
    }

    private static String prettyPrint(final Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter -> String.format(
                    "CALLABLE %s (%s) : %s",
                    funcParameter.name(),
                    makeDelimited(funcParameter.parameters(), ASTPrinter::prettyPrint),
                    prettyPrint(funcParameter.declaredReturnType())
            );
            case Parameter.VarParameter varParameter -> varParameter.name() + " : " + prettyPrint(varParameter.declaredType());
            case Parameter.ValueParameter valueParameter -> String.format(
                    "%s : %s",
                    valueParameter.name(),
                    prettyPrint(valueParameter.declaredType())
            );
        };
    }

    private static String prettyPrint(final TypeSig typeSig) {
        return "'" + switch (typeSig) {
            case TypeSig.ArrayTypeSig arrayType -> String.format(
                    "%s[%d]",
                    prettyPrint(arrayType.elementTypeSig()),
                    arrayType.arraySize()
            );
            case TypeSig.RecordTypeSig recordType -> String.format(
                    "RECORD {%s}",
                    makeDelimited(recordType.fieldTypes(), t -> t.fieldName() + " : " + prettyPrint(t.fieldTypeSig()))
            );
            case TypeSig.BasicTypeSig basicType -> basicType.name();
            case TypeSig.Void _ -> "VOID";
        };
    }

    private static String prettyPrint(final Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> String.format(
                    "%s[%s]",
                    prettyPrint(arraySubscript.array()),
                    prettyPrint(arraySubscript.subscript())
            );
            case Expression.Identifier.BasicIdentifier basicIdentifier -> "|" + basicIdentifier.name() + "|";
            case Expression.Identifier.RecordAccess recordAccess -> String.format(
                    "%s.%s",
                    prettyPrint(recordAccess.record()),
                    prettyPrint(recordAccess.field())
            );
        };
    }

    private static <T> String makeDelimited(List<T> elems, Function<? super T, String> traverser) {

        if (elems.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < elems.size() - 1; i++) {
            sb.append(traverser.apply(elems.get(i)));
            sb.append(", ");
        }

        sb.append(traverser.apply(elems.getLast()));

        return sb.toString();
    }

    private ASTPrinter() {
        throw new IllegalStateException("Utility class");
    }
}
