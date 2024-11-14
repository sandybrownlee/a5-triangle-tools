package triangle.util;

import triangle.repr.Argument;
import triangle.repr.Declaration;
import triangle.repr.Expression;
import triangle.repr.Parameter;
import triangle.repr.Statement;
import triangle.repr.TypeSig;

public final class ASTPrinter {

    public String prettyPrint(final Statement statement) {
        return switch (statement) {
            case Statement.ExpressionStatement expression -> prettyPrint(expression.expression());
            case Statement.AssignStatement assignStatement -> String.format(
                    "%s := %s",
                    prettyPrint(assignStatement.identifier()),
                    prettyPrint(assignStatement.expression())
            );
            case Statement.IfStatement ifStatement -> String.format(
                    "IF %s THEN %s ELSE %s",
                    prettyPrint(ifStatement.condition()),
                    ifStatement.consequent().map(this::prettyPrint),
                    ifStatement.alternative().map(this::prettyPrint)
            );
            case Statement.LetStatement letStatement -> String.format(
                    "LET {%s} IN %s",
                    letStatement.declarations().stream().map(d -> prettyPrint(d) + ",").reduce("", String::concat),
                    prettyPrint(letStatement.statement())
            );
            case Statement.LoopWhileStatement loopWhileStatement -> String.format(
                    "LOOP %s WHILE (%s) DO %s",
                    prettyPrint(loopWhileStatement.loopBody()),
                    prettyPrint(loopWhileStatement.condition()),
                    prettyPrint(loopWhileStatement.doBody())
            );
            case Statement.RepeatUntilStatement repeatUntilStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    prettyPrint(repeatUntilStatement.body()),
                    prettyPrint(repeatUntilStatement.condition())
            );
            case Statement.RepeatWhileStatement repeatWhileStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    prettyPrint(repeatWhileStatement.body()),
                    prettyPrint(repeatWhileStatement.condition())
            );
            case Statement.StatementBlock statementBlock -> String.format(
                    "BEGIN {%s} END",
                    statementBlock.statements().stream().map(s -> prettyPrint(s) + ",").reduce("", String::concat)
            );
            case Statement.WhileStatement whileStatement -> String.format(
                    "WHILE %s DO %s",
                    prettyPrint(whileStatement.condition()),
                    prettyPrint(whileStatement.body())
            );
        };
    }

    private String prettyPrint(final Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument -> prettyPrint(funcArgument.func());
            case Argument.VarArgument varArgument -> prettyPrint(varArgument.var());
            case Expression expression -> prettyPrint(expression);
        };
    }

    private String prettyPrint(final Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration -> String.format(
                    "CONST %s = %s",
                    constDeclaration.name(),
                    prettyPrint(constDeclaration.value())
            );
            case Declaration.FuncDeclaration funcDeclaration -> String.format(
                    "FUNC %s (%s) {%s} %s ",
                    funcDeclaration.name(),
                    funcDeclaration.parameters().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    prettyPrint(funcDeclaration.expression()),
                    prettyPrint(funcDeclaration.returnTypeSig())
            );
            case Declaration.TypeDeclaration typeDeclaration -> String.format(
                    "%s : %s",
                    typeDeclaration.name(),
                    prettyPrint(typeDeclaration.typeSig())
            );
            case Declaration.VarDeclaration varDeclaration -> String.format("VAR %s", varDeclaration.name());
            case Declaration.ProcDeclaration procDeclaration -> String.format(
                    "PROC %s (%s) {%s}",
                    procDeclaration.name(),
                    procDeclaration.parameters().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    prettyPrint(procDeclaration.statement())
            );
        };
    }

    private String prettyPrint(final Expression expression) {
        return "(" + switch (expression) {
            case Expression.BinaryOp binaryOp -> String.format(
                    "BINOP %s %s %s",
                    binaryOp.operator(),
                    prettyPrint(binaryOp.leftOperand()),
                    prettyPrint(binaryOp.rightOperand())
            );
            case Expression.FunCall funCall -> String.format(
                    "FUNCALL %s (%s)",
                    prettyPrint(funCall.func()),
                    funCall.arguments().stream().map(a -> prettyPrint(a) + ",").reduce("", String::concat)
            );
            case Expression.Identifier identifier -> prettyPrint(identifier);
            case Expression.IfExpression ifExpression -> String.format(
                    "IF %s THEN %s ELSE %s",
                    prettyPrint(ifExpression.condition()),
                    prettyPrint(ifExpression.consequent()),
                    prettyPrint(ifExpression.alternative())
            );
            case Expression.LetExpression letExpression -> String.format(
                    "LET {%s} IN %s",
                    letExpression.declarations().stream().map(d -> prettyPrint(d) + ",").reduce("", String::concat),
                    prettyPrint(letExpression.expression())
            );
            case Expression.LitArray litArray -> String.format(
                    "[%s]",
                    litArray.elements().stream().map(v -> prettyPrint(v) + ",").reduce("", String::concat)
            );
            case Expression.LitChar litChar -> Character.toString(litChar.value());
            case Expression.LitInt litInt -> Integer.toString(litInt.value());
            case Expression.LitRecord litRecord -> String.format(
                    "{%s}",
                    litRecord.fields().stream().map(v -> v.name() + " = " + prettyPrint(v.value()) + ",")
                             .reduce("", String::concat)
            );
            case Expression.UnaryOp unaryOp -> String.format(
                    "UNARYOP %s %s",
                    unaryOp.operator(),
                    prettyPrint(unaryOp.operand())
            );
            case Expression.LitBool litBool -> Boolean.toString(litBool.value());
            case Expression.SequenceExpression sequenceExpression -> String.format(
                    "AFTER %s RETURN %s",
                    sequenceExpression.statement(),
                    sequenceExpression.expression()
            );
        } + ")";
    }

    private String prettyPrint(final Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter -> String.format(
                    "CALLABLE %s (%s) : %s",
                    funcParameter.callable(),
                    funcParameter.parameters().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    prettyPrint(funcParameter.declaredReturnType())
            );
            case Parameter.VarParameter varParameter -> varParameter.name() + ":" + prettyPrint(varParameter.declaredType());
            case Parameter.ValueParameter valueParameter -> String.format(
                    "%s : %s",
                    valueParameter.name(),
                    prettyPrint(valueParameter.declaredType())
            );
        };
    }

    private String prettyPrint(final TypeSig typeSig) {
        return "TYPE " + switch (typeSig) {
            case TypeSig.ArrayTypeSig arrayType -> String.format(
                    "%s[%d]",
                    prettyPrint(arrayType.elementTypeSig()),
                    arrayType.arraySize()
            );
            case TypeSig.RecordTypeSig recordType -> String.format(
                    "RECORD {%s}",
                    recordType.fieldTypes().stream().map(t -> t.fieldName() + " : " + prettyPrint(t.fieldTypeSig()) + ",")
            );
            case TypeSig.BasicTypeSig basicType -> basicType.name();
            case TypeSig.Void _ -> "VOID";
        };
    }

    private String prettyPrint(final Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> String.format(
                    "%s[%s]",
                    prettyPrint(arraySubscript.array()),
                    prettyPrint(arraySubscript.subscript())
            );
            case Expression.Identifier.BasicIdentifier basicIdentifier -> basicIdentifier.name();
            case Expression.Identifier.RecordAccess recordAccess -> String.format(
                    "%s.%s",
                    prettyPrint(recordAccess.record()),
                    prettyPrint(recordAccess.field())
            );
        };
    }

}
