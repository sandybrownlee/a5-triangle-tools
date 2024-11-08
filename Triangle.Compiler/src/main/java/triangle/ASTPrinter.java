package triangle;

import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Statement;
import triangle.types.Type;

public final class ASTPrinter {

    public String prettyPrint(final Statement statement) {
        return switch (statement) {
            case Expression expression -> prettyPrint(expression);
            case Statement.AssignStatement assignStatement -> String.format(
                    "%s := %s",
                    prettyPrint(assignStatement.identifier()),
                    prettyPrint(assignStatement.expression())
            );
            case Statement.IfStatement ifStatement -> String.format(
                    "IF %s THEN %s ELSE %s",
                    prettyPrint(ifStatement.condition()),
                    ifStatement.consequent().map(x -> prettyPrint(x)),
                    ifStatement.alternative().map(x -> prettyPrint(x))
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
                    "CALLABLE %s (%s) {%s} %s ",
                    funcDeclaration.func(),
                    funcDeclaration.parameters().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    prettyPrint(funcDeclaration.expression()),
                    prettyPrint(funcDeclaration.returnType())
            );
            case Declaration.TypeDeclaration typeDeclaration -> String.format(
                    "%s : %s",
                    typeDeclaration.name(),
                    prettyPrint(typeDeclaration.type())
            );
            case Declaration.VarDeclaration varDeclaration -> String.format("VAR %s", varDeclaration.name());
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
                    prettyPrint(funCall.callable()),
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
        } + ")";
    }

    private String prettyPrint(final Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter -> String.format(
                    "CALLABLE %s (%s) : %s",
                    funcParameter.callable(),
                    funcParameter.parameters().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    prettyPrint(funcParameter.returnType())
            );
            case Parameter.VarParameter varParameter -> varParameter.name() + ":" + prettyPrint(varParameter.type());
            case Parameter.ConstParameter constParameter -> String.format(
                    "CONST %s : %s",
                    constParameter.getName(),
                    prettyPrint(constParameter.type())
            );
        };
    }

    private String prettyPrint(final Type type) {
        return "TYPE " + switch (type) {
            case Type.ArrayType arrayType -> String.format(
                    "%s[%d]",
                    prettyPrint(arrayType.elementType()),
                    arrayType.size()
            );
            case Type.RecordType recordType -> String.format(
                    "RECORD {%s}",
                    recordType.fieldTypes().stream().map(t -> t.fieldName() + " : " + prettyPrint(t.fieldType()) + ",")
            );
            case Type.BasicType basicType -> basicType.name();
            case Type.PrimType.VoidType _ -> "VOID";
            case Type.PrimType.BoolType boolType -> "BOOL";
            case Type.PrimType.CharType charType -> "CHAR";
            // will be needed for custom function type definitions
            case Type.PrimType.FuncType funcType -> String.format(
                    "FUNC (%s) : %s",
                    funcType.argTypes().stream().map(p -> prettyPrint(p) + ",").reduce("", String::concat),
                    funcType.returnType()
            );
            case Type.PrimType.IntType intType -> "INT";
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
