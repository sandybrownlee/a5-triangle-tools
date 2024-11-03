package triangle;

import triangle.ast.AllVisitor;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Parameter;
import triangle.ast.Program;
import triangle.ast.Statement;
import triangle.ast.Type;

public final class ASTPrinter implements AllVisitor<Void, String, Exception> {

    @Override public String visit(final Void state, final Argument argument) {
        return switch (argument) {
            case Argument.FuncArgument funcArgument -> visit(state, funcArgument.func());
            case Argument.VarArgument varArgument -> visit(state, varArgument.var());
            case Expression expression -> visit(state, expression);
        };
    }

    @Override public String visit(final Void state, final Declaration declaration) {
        return switch (declaration) {
            case Declaration.ConstDeclaration constDeclaration -> String.format(
                    "CONST %s = %s",
                    constDeclaration.name(),
                    visit(state, constDeclaration.value())
            );
            case Declaration.FuncDeclaration funcDeclaration -> String.format(
                    "CALLABLE %s (%s) {%s} %s ",
                    funcDeclaration.func(),
                    funcDeclaration.parameters().stream().map(p -> visit(state, p) + ",").reduce("", String::concat),
                    visit(state, funcDeclaration.expression()),
                    visit(state, funcDeclaration.returnType())
            );
            case Declaration.TypeDeclaration typeDeclaration -> String.format(
                    "%s : %s",
                    typeDeclaration.name(),
                    visit(state, typeDeclaration.type())
            );
            case Declaration.VarDeclaration varDeclaration -> String.format("VAR %s", varDeclaration.name());
        };
    }

    @Override public String visit(final Void state, final Expression expression) {
        return "(" + switch (expression) {
            case Expression.BinaryOp binaryOp -> String.format(
                    "BINOP %s %s %s",
                    binaryOp.operator(),
                    visit(state, binaryOp.leftOperand()),
                    visit(state, binaryOp.rightOperand())
            );
            case Expression.FunCall funCall -> String.format(
                    "FUNCALL %s (%s)",
                    visit(state, funCall.callable()),
                    funCall.arguments().stream().map(a -> visit(state, a) + ",").reduce("", String::concat)
            );
            case Expression.Identifier identifier -> visit(state, identifier);
            case Expression.IfExpression ifExpression -> String.format(
                    "IF %s THEN %s ELSE %s",
                    visit(state, ifExpression.condition()),
                    visit(state, ifExpression.consequent()),
                    visit(state, ifExpression.alternative())
            );
            case Expression.LetExpression letExpression -> String.format(
                    "LET {%s} IN %s",
                    letExpression.declarations().stream().map(d -> visit(state, d) + ",").reduce("", String::concat),
                    visit(state, letExpression.expression())
            );
            case Expression.LitArray litArray -> String.format(
                    "[%s]",
                    litArray.elements().stream().map(v -> visit(state, v) + ",").reduce("", String::concat)
            );
            case Expression.LitChar litChar -> Character.toString(litChar.value());
            case Expression.LitInt litInt -> Integer.toString(litInt.value());
            case Expression.LitRecord litRecord -> String.format(
                    "{%s}",
                    litRecord.fields().stream().map(v -> v.name() + " = " + visit(state, v.value()) + ",")
                             .reduce("", String::concat)
            );
            case Expression.UnaryOp unaryOp -> String.format(
                    "UNARYOP %s %s",
                    unaryOp.operator(),
                    visit(state, unaryOp.operand())
            );
            case Expression.LitBool litBool -> Boolean.toString(litBool.value());
        } + ")";
    }

    @Override public String visit(final Void state, final Parameter parameter) {
        return switch (parameter) {
            case Parameter.FuncParameter funcParameter -> String.format(
                    "CALLABLE %s (%s) : %s",
                    funcParameter.callable(),
                    funcParameter.parameters().stream().map(p -> visit(state, p) + ",").reduce("", String::concat),
                    visit(state, funcParameter.returnType())
            );
            case Parameter.VarParameter varParameter -> varParameter.name() + ":" + visit(state, varParameter.type());
        };
    }

    @Override public String visit(final Void state, final Program program) {
        return program.statements().stream().map(s -> visit(state, s) + ",").reduce("", String::concat);
    }

    @Override public String visit(final Void state, final Statement statement) {
        return switch (statement) {
            case Expression expression -> visit(state, expression);
            case Statement.AssignStatement assignStatement -> String.format(
                    "%s := %s",
                    visit(state, assignStatement.identifier()),
                    visit(state, assignStatement.expression())
            );
            case Statement.IfStatement ifStatement -> String.format(
                    "IF %s THEN %s ELSE %s",
                    visit(state, ifStatement.condition()),
                    ifStatement.consequent().map(x -> visit(state, x)),
                    ifStatement.alternative().map(x -> visit(state, x))
            );
            case Statement.LetStatement letStatement -> String.format(
                    "LET {%s} IN %s",
                    letStatement.declarations().stream().map(d -> visit(state, d) + ",").reduce("", String::concat),
                    visit(state, letStatement.statement())
            );
            case Statement.LoopWhileStatement loopWhileStatement -> String.format(
                    "LOOP %s WHILE (%s) DO %s",
                    visit(state, loopWhileStatement.loopBody()),
                    visit(state, loopWhileStatement.condition()),
                    visit(state, loopWhileStatement.doBody())
            );
            case Statement.RepeatUntilStatement repeatUntilStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    visit(state, repeatUntilStatement.body()),
                    visit(state, repeatUntilStatement.condition())
            );
            case Statement.RepeatWhileStatement repeatWhileStatement -> String.format(
                    "REPEAT %s UNTIL %s",
                    visit(state, repeatWhileStatement.body()),
                    visit(state, repeatWhileStatement.condition())
            );
            case Statement.StatementBlock statementBlock -> String.format(
                    "BEGIN {%s} END",
                    statementBlock.statements().stream().map(s -> visit(state, s) + ",").reduce("", String::concat)
            );
            case Statement.WhileStatement whileStatement -> String.format(
                    "WHILE %s DO %s",
                    visit(state, whileStatement.condition()),
                    visit(state, whileStatement.body())
            );
        };
    }

    @Override public String visit(final Void state, final Type type) {
        return "TYPE " + switch (type) {
            case Type.ArrayType arrayType -> String.format(
                    "%s[%d]",
                    visit(state, arrayType.elementType()),
                    arrayType.size()
            );
            case Type.RecordType recordType -> String.format(
                    "RECORD {%s}",
                    recordType.fieldTypes().stream().map(t -> t.fieldName() + " : " + visit(state, t.fieldType()) + ",")
            );
            case Type.BasicType basicType -> basicType.name();
            case Type.VoidType _ -> "VOID";
            case Type.BoolType boolType -> "BOOL";
            case Type.CharType charType -> "CHAR";
            // will be needed for custom function type definitions
            case Type.FuncType funcType -> String.format(
                    "FUNC (%s) : %s",
                    funcType.argTypes().stream().map(p -> visit(state, p) + ",").reduce("", String::concat),
                    funcType.returnType()
            );
            case Type.IntType intType -> "INT";
        };
    }

    @Override public String visit(final Void state, final Expression.Identifier identifier) {
        return switch (identifier) {
            case Expression.Identifier.ArraySubscript arraySubscript -> String.format(
                    "%s[%s]",
                    visit(state, arraySubscript.array()),
                    visit(state, arraySubscript.subscript())
            );
            case Expression.Identifier.BasicIdentifier basicIdentifier -> basicIdentifier.name();
            case Expression.Identifier.RecordAccess recordAccess -> String.format(
                    "%s.%s",
                    visit(state, recordAccess.record()),
                    visit(state, recordAccess.field())
            );
        };
    }

}
