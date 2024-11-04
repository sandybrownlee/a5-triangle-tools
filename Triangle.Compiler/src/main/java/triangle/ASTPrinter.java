package triangle;

import triangle.ast.AllVisitor;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Program;
import triangle.ast.Statement;
import triangle.ast.Type;

public final class ASTPrinter implements AllVisitor<String, Exception> {

    @Override public String visit(final Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument -> visit(funcArgument.func());
            case Argument.VarArgument varArgument -> visit(varArgument.var());
            case Expression expression -> visit(expression);
        };
    }

    @Override public String visit(final Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration -> String.format(
                    "CONST %s = %s",
                    constDeclaration.name(),
                    visit(constDeclaration.value())
            );
            case Declaration.FuncDeclaration funcDeclaration -> String.format(
                    "CALLABLE %s (%s) {%s} %s ",
                    funcDeclaration.func(),
                    funcDeclaration.parameters().stream().map(p -> visit(p) + ",").reduce("", String::concat),
                    visit(funcDeclaration.expression()),
                    visit(funcDeclaration.returnType())
            );
            case Declaration.TypeDeclaration typeDeclaration -> String.format(
                    "%s : %s",
                    typeDeclaration.name(),
                    visit(typeDeclaration.type())
            );
            case Declaration.VarDeclaration varDeclaration -> String.format("VAR %s", varDeclaration.name());
        };
    }

    @Override public String visit(final Expression expression) {
        return "(" + switch (expression) {
            case Expression.BinaryOp binaryOp -> String.format(
                    "BINOP %s %s %s",
                    binaryOp.operator(),
                    visit(binaryOp.leftOperand()),
                    visit(binaryOp.rightOperand())
            );
            case Expression.FunCall funCall -> String.format(
                    "FUNCALL %s (%s)",
                    visit(funCall.callable()),
                    funCall.arguments().stream().map(a -> visit(a) + ",").reduce("", String::concat)
            );
            case Expression.Identifier identifier -> visit(identifier);
            case Expression.IfExpression ifExpression -> String.format(
                    "IF %s THEN %s ELSE %s",
                    visit(ifExpression.condition()),
                    visit(ifExpression.consequent()),
                    visit(ifExpression.alternative())
            );
            case Expression.LetExpression letExpression -> String.format(
                    "LET {%s} IN %s",
                    letExpression.declarations().stream().map(d -> visit(d) + ",").reduce("", String::concat),
                    visit(letExpression.expression())
            );
            case Expression.LitArray litArray -> String.format(
                    "[%s]",
                    litArray.elements().stream().map(v -> visit(v) + ",").reduce("", String::concat)
            );
            case Expression.LitChar litChar -> Character.toString(litChar.value());
            case Expression.LitInt litInt -> Integer.toString(litInt.value());
            case Expression.LitRecord litRecord -> String.format(
                    "{%s}",
                    litRecord.fields().stream().map(v -> v.name() + " = " + visit(v.value()) + ",")
                             .reduce("", String::concat)
            );
            case Expression.UnaryOp unaryOp -> String.format(
                    "UNARYOP %s %s",
                    unaryOp.operator(),
                    visit(unaryOp.operand())
            );
            case Expression.LitBool litBool -> Boolean.toString(litBool.value());
        } + ")";
    }

    @Override public String visit(final Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter -> String.format(
                    "CALLABLE %s (%s) : %s",
                    funcParameter.callable(),
                    funcParameter.parameters().stream().map(p -> visit(p) + ",").reduce("", String::concat),
                    visit(funcParameter.returnType())
            );
            case Parameter.VarParameter varParameter -> varParameter.name() + ":" + visit(varParameter.type());
            case Parameter.ConstParameter constParameter -> String.format(
                    "CONST %s : %s",
                    constParameter.getName(),
                    visit(constParameter.type())
            );
        };
    }

    @Override public String visit(final Program program) {
        return program.statements().stream().map(s -> visit(s) + ",").reduce("", String::concat);
    }

    @Override public String visit(final Statement statement) {
        return switch (statement) {
            case Expression expression -> visit(expression);
            case Statement.AssignStatement assignStatement -> String.format(
                    "%s := %s",
                    visit(assignStatement.identifier()),
                    visit(assignStatement.expression())
            );
            case Statement.IfStatement ifStatement -> String.format(
                    "IF %s THEN %s ELSE %s",
                    visit(ifStatement.condition()),
                    ifStatement.consequent().map(x -> visit(x)),
                    ifStatement.alternative().map(x -> visit(x))
            );
            case Statement.LetStatement letStatement -> String.format(
                    "LET {%s} IN %s",
                    letStatement.declarations().stream().map(d -> visit(d) + ",").reduce("", String::concat),
                    visit(letStatement.statement())
            );
            case Statement.LoopWhileStatement loopWhileStatement -> String.format(
                    "LOOP %s WHILE (%s) DO %s",
                    visit(loopWhileStatement.loopBody()),
                    visit(loopWhileStatement.condition()),
                    visit(loopWhileStatement.doBody())
            );
            case Statement.RepeatUntilStatement repeatUntilStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    visit(repeatUntilStatement.body()),
                    visit(repeatUntilStatement.condition())
            );
            case Statement.RepeatWhileStatement repeatWhileStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    visit(repeatWhileStatement.body()),
                    visit(repeatWhileStatement.condition())
            );
            case Statement.StatementBlock statementBlock -> String.format(
                    "BEGIN {%s} END",
                    statementBlock.statements().stream().map(s -> visit(s) + ",").reduce("", String::concat)
            );
            case Statement.WhileStatement whileStatement -> String.format(
                    "WHILE %s DO %s",
                    visit(whileStatement.condition()),
                    visit(whileStatement.body())
            );
        };
    }

    @Override public String visit(final Type type) {
        return "TYPE " + switch (type) {
            case Type.ArrayType arrayType -> String.format(
                    "%s[%d]",
                    visit(arrayType.elementType()),
                    arrayType.size()
            );
            case Type.RecordType recordType -> String.format(
                    "RECORD {%s}",
                    recordType.fieldTypes().stream().map(t -> t.fieldName() + " : " + visit(t.fieldType()) + ",")
            );
            case Type.BasicType basicType -> basicType.name();
            case Type.PrimType.VoidType _ -> "VOID";
            case Type.PrimType.BoolType boolType -> "BOOL";
            case Type.PrimType.CharType charType -> "CHAR";
            // will be needed for custom function type definitions
            case Type.PrimType.FuncType funcType -> String.format(
                    "FUNC (%s) : %s",
                    funcType.argTypes().stream().map(p -> visit(p) + ",").reduce("", String::concat),
                    funcType.returnType()
            );
            case Type.PrimType.IntType intType -> "INT";
        };
    }

    @Override public String visit(final Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> String.format(
                    "%s[%s]",
                    visit(arraySubscript.array()),
                    visit(arraySubscript.subscript())
            );
            case Expression.Identifier.BasicIdentifier basicIdentifier -> basicIdentifier.name();
            case Expression.Identifier.RecordAccess recordAccess -> String.format(
                    "%s.%s",
                    visit(recordAccess.record()),
                    visit(recordAccess.field())
            );
        };
    }

}
