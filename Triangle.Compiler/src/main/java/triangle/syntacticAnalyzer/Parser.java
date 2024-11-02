/*
 * @(#)Parser.java
 *
 * Revisions and updates (c) 2024 Zaraksh Rahman. zar00024@students.stir.ac.uk
 * Revisions and updates (c) 2022-2024 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 *
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle.syntacticAnalyzer;

import triangle.ast.AST.*;
import triangle.ast.Argument;
import triangle.ast.Declaration;
import triangle.ast.Expression;
import triangle.ast.Expression.*;
import triangle.ast.Parameter;
import triangle.ast.Parameter.*;
import triangle.ast.Statement;
import triangle.ast.Statement.*;
import triangle.ast.Type;
import triangle.ast.Type.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Parser {

    private static final Set<Token.Kind> EXPRESSION_FIRST_SET  = new HashSet<>();
    private static final Set<Token.Kind> STATEMENT_FIRST_SET   = new HashSet<>();
    private static final Set<Token.Kind> DECLARATION_FIRST_SET = new HashSet<>();

    // manual transitive closure for FIRST sets of ident, expr, and stmt
    static {
        DECLARATION_FIRST_SET.addAll(Set.of(Token.Kind.CONST, Token.Kind.VAR, Token.Kind.PROC, Token.Kind.FUNC, Token.Kind.TYPE));

        EXPRESSION_FIRST_SET.addAll(Set.of(
                Token.Kind.INTLITERAL, Token.Kind.CHARLITERAL, Token.Kind.LBRACK, Token.Kind.LBRACE, Token.Kind.LPAREN,
                Token.Kind.LET, Token.Kind.IF, Token.Kind.IDENTIFIER, Token.Kind.OPERATOR
        ));

        STATEMENT_FIRST_SET.addAll(
                Set.of(Token.Kind.BEGIN, Token.Kind.LET, Token.Kind.IF, Token.Kind.WHILE, Token.Kind.LOOP, Token.Kind.REPEAT, Token.Kind.IDENTIFIER));
        STATEMENT_FIRST_SET.addAll(EXPRESSION_FIRST_SET);
    }

    private final Lexer lexer;
    private       Token lastToken;

    public Parser(Lexer lexer, Object ignored) {
        this.lexer = lexer;
    }

    public Program parseProgram() throws IOException, SyntaxError {
        shift();
        return new Program(parseStmtSeq());
    }

    private List<Statement> parseStmtSeq() throws SyntaxError, IOException {
        List<Statement> statements = new ArrayList<>();
        statements.add(parseStmt());


        if (lastToken.getKind() == Token.Kind.SEMICOLON) {
            shift(); // SEMICOLON
        }

        if (STATEMENT_FIRST_SET.contains(lastToken.getKind())) {
            statements.addAll(parseStmtSeq());
        }

        return statements;
    }

    private Statement parseStmt() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case BEGIN -> {
                shift(); // BEGIN
                List<Statement> statements = parseStmtSeq();
                shift(); // END
                yield new StatementBlock(statements);
            }
            case LET -> {
                shift(); // LET
                List<Declaration> declarations = parseDeclSeq();
                shift(); // IN
                Statement statement = parseStmt();
                yield new LetStatement(declarations, statement);
            }
            case IF -> {
                shift(); // IF
                Expression condition = parseExpression();
                shift(); // THEN

                Optional<Statement> consequent =
                        (lastToken.getKind() == Token.Kind.ELSE) ? Optional.empty() : Optional.of(parseStmt());
                shift(); // ELSE

                // else branches are allowed to end in SEMICOLON, another statement, or nothing at all
                Optional<Statement> alternative = switch (lastToken.getKind()) {
                    case SEMICOLON -> {
                        shift(); // SEMICOLON
                        yield Optional.empty();
                    }
                    case Token.Kind k when STATEMENT_FIRST_SET.contains(k) -> Optional.of(parseStmt());
                    // anything that can't start a statement is assumed to be a skipped else branch
                    default -> Optional.empty();
                };

                yield new IfStatement(condition, consequent, alternative);
            }
            case WHILE -> {
                shift(); // WHILE
                Expression condition = parseExpression();
                shift(); // DO
                Statement statement = parseStmt();
                yield new WhileStatement(condition, statement);
            }
            case LOOP -> {
                shift(); // LOOP
                Statement statement1 = parseStmt();
                shift(); // WHILE
                Expression condition = parseExpression();
                shift(); // DO
                Statement statement2 = parseStmt();
                yield new LoopWhileStatement(condition, statement1, statement2);
            }
            case REPEAT -> {
                shift(); // REPEAT
                Statement statement = parseStmt();
                if (lastToken.getKind() == Token.Kind.WHILE) {
                    shift(); // WHILE
                    Expression condition = parseExpression();
                    yield new RepeatWhileStatement(condition, statement);
                } else {
                    shift(); // UNTIL
                    Expression condition = parseExpression();
                    yield new RepeatUntilStatement(condition, statement);
                }
            }
            case IDENTIFIER -> {
                Identifier identifier = parseIdentifier();
                if (lastToken.getKind() == Token.Kind.BECOMES) {
                    shift(); // BECOMES
                    Expression expression = parseExpression();
                    yield new AssignStatement(identifier, expression);
                }

                // check if the identifier leads into a call
                if (lastToken.getKind() == Token.Kind.LPAREN) {
                    shift(); // LPAREN
                    @SuppressWarnings("unchecked") List<Argument> arguments =
                            (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseArgSeq();
                    shift(); // RPAREN
                    yield new CallExpression(identifier, arguments);
                }

                // check if the identifier leads into a side-effectful operation
                if (lastToken.getKind() == Token.Kind.OPERATOR) {
                    String operator = ((TextToken) lastToken).getText();
                    shift(); // OPERATOR

                    if (EXPRESSION_FIRST_SET.contains(lastToken.getKind())) {
                        Expression secondExpression = parseExpression();
                        yield new BinaryOp(operator, identifier, secondExpression);
                    }

                    yield new UnaryOp(operator, identifier);
                }

                yield identifier;
            }
            case Token.Kind k when EXPRESSION_FIRST_SET.contains(k) -> parseExpression();
            default -> throw new RuntimeException();
        };
    }

    private Expression parseExpression() throws IOException, SyntaxError {
        Expression firstExpression = switch (lastToken.getKind()) {
            case INTLITERAL -> {
                LitInt litInt = new LitInt(Integer.parseInt(((TextToken) lastToken).getText()));
                shift(); // INTLITERAL
                yield litInt;
            }
            case CHARLITERAL -> {
                LitChar litChar = new LitChar(((TextToken) lastToken).getText().charAt(0));
                shift(); // CHARLITERAL
                yield litChar;
            }
            case LBRACK -> {
                shift(); // LBRACK
                @SuppressWarnings("unchecked") List<Expression> arrayValues =
                        (lastToken.getKind() == Token.Kind.RBRACK) ? Collections.EMPTY_LIST : parseArraySeq();
                shift(); // RBRACK
                yield new LitArray(arrayValues);
            }
            case LBRACE -> {
                shift(); // LBRACE
                @SuppressWarnings("unchecked") List<LitRecord.RecordField> fields =
                        (lastToken.getKind() == Token.Kind.RBRACE) ? Collections.EMPTY_LIST : parseFieldSeq();
                shift(); // RBRACE
                yield new LitRecord(fields);
            }
            case LPAREN -> {
                shift(); // LPAREN
                Expression expression = parseExpression();
                shift(); // RPAREN
                yield expression;
            }
            case LET -> {
                shift(); // LET
                List<Declaration> declarations = parseDeclSeq();
                shift(); // IN
                Expression expression = parseExpression();
                yield new LetExpression(declarations, expression);
            }
            case IF -> {
                shift(); // IF
                Expression condition = parseExpression();
                shift(); // THEN
                Expression consequent = parseExpression();
                shift(); // ELSE
                Expression alternative = parseExpression();
                yield new IfExpression(condition, consequent, alternative);
            }
            case IDENTIFIER -> {
                Identifier identifier = parseIdentifier();

                if (lastToken.getKind() == Token.Kind.LPAREN) {
                    shift(); // LPAREN
                    @SuppressWarnings("unchecked") List<Argument> arguments =
                            (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseArgSeq();
                    shift(); // RPAREN
                    yield new CallExpression(identifier, arguments);
                }

                yield identifier;
            }
            // unary prefix op
            case OPERATOR -> {
                String operator = ((TextToken) lastToken).getText();
                shift(); // OPERATOR
                Expression expression = parseExpression();
                yield new UnaryOp(operator, expression);
            }

            default -> throw new RuntimeException();
        };

        if (lastToken.getKind() == Token.Kind.OPERATOR) {
            String operator = ((TextToken) lastToken).getText();
            shift(); // OPERATOR
            if (EXPRESSION_FIRST_SET.contains(lastToken.getKind())) {
                Expression secondExpression = parseExpression();
                return new BinaryOp(operator, firstExpression, secondExpression);
            } else {
                return new UnaryOp(operator, firstExpression);
            }
        }

        return firstExpression;
    }

    private Identifier parseIdentifier() throws IOException, SyntaxError {
        Identifier identifier = new Identifier.BasicIdentifier(((TextToken) lastToken).getText());

        shift(); // IDENTIFIER
        while (lastToken.getKind() == Token.Kind.DOT || lastToken.getKind() == Token.Kind.LBRACK) {
            if (lastToken.getKind() == Token.Kind.DOT) {
                shift(); // DOT
                Identifier recordField = parseIdentifier();
                identifier = new Identifier.RecordAccess(identifier, recordField);
            } else if (lastToken.getKind() == Token.Kind.LBRACK) {
                shift(); // LBRACK
                Identifier arraySubscript = parseIdentifier();
                shift(); // RBRACK
                identifier = new Identifier.ArraySubscript(identifier, arraySubscript);
            }
        }

        return identifier;
    }

    private Type parseType() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case IDENTIFIER -> {
                TypeIdentifier type = new TypeIdentifier(((TextToken) lastToken).getText());
                shift(); // IDENTIFIER
                yield type;
            }
            case ARRAY -> {
                shift(); // ARRAY
                int size = Integer.parseInt(((TextToken) lastToken).getText());
                shift(); // INTLITERAL
                shift(); // OF
                Type elementType = parseType();
                yield new ArrayType(size, elementType);
            }
            case RECORD -> {
                shift(); // RECORD
                @SuppressWarnings("unchecked") List<RecordType.RecordFieldType> fieldTypes =
                        (lastToken.getKind() == Token.Kind.END) ? Collections.EMPTY_LIST : parseFieldTypeSeq();
                shift(); // END
                yield new RecordType(fieldTypes);
            }
            default -> throw new RuntimeException();
        };
    }

    private List<Argument> parseArgSeq() throws IOException, SyntaxError {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(parseArg());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(); // COMMA
                arguments.add(parseArg());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return arguments;
    }

    private Argument parseArg() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case PROC, FUNC -> {
                shift(); // PROC
                String callableName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                yield new Argument.FuncArgument(callableName);
            }
            case VAR -> {
                shift(); // VAR
                yield new Argument.VarArgument(parseIdentifier());
            }
            case Token.Kind k when EXPRESSION_FIRST_SET.contains(k) -> new Argument.ExprArgument(parseExpression());
            default -> throw new RuntimeException();
        };
    }

    private List<Expression> parseArraySeq() throws IOException, SyntaxError {
        List<Expression> arrayValues = new ArrayList<>();
        arrayValues.add(parseExpression());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(); // COMMA
                arrayValues.add(parseExpression());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return arrayValues;
    }

    private List<LitRecord.RecordField> parseFieldSeq() throws IOException, SyntaxError {
        List<LitRecord.RecordField> recordFields = new ArrayList<>();
        recordFields.add(parseField());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(); // COMMA
                recordFields.add(parseField());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return recordFields;
    }

    private LitRecord.RecordField parseField() throws IOException, SyntaxError {
        String fieldName = ((TextToken) lastToken).getText();
        shift(); // IDENTIFIER
        shift(); // IS
        return new LitRecord.RecordField(fieldName, parseExpression());
    }

    private List<RecordType.RecordFieldType> parseFieldTypeSeq() throws IOException, SyntaxError {
        List<RecordType.RecordFieldType> recordFieldTypes = new ArrayList<>();
        recordFieldTypes.add(parseFieldType());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(); // COMMA
                recordFieldTypes.add(parseFieldType());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return recordFieldTypes;
    }

    private RecordType.RecordFieldType parseFieldType() throws IOException, SyntaxError {
        String fieldName = ((TextToken) lastToken).getText();
        shift(); // IDENTIFIER
        shift(); // COLON
        Type fieldType = parseType();
        return new RecordType.RecordFieldType(fieldName, fieldType);
    }

    private List<Declaration> parseDeclSeq() throws IOException, SyntaxError {
        List<Declaration> declarations = new ArrayList<>();
        declarations.add(parseDecl());

        if (lastToken.getKind() == Token.Kind.SEMICOLON) {
            shift(); // SEMICOLON
        }

        if (DECLARATION_FIRST_SET.contains(lastToken.getKind())) {
            declarations.addAll(parseDeclSeq());
        }

        return declarations;
    }

    private Declaration parseDecl() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case CONST -> {
                shift(); // CONST
                String constName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // IS
                Expression expression = parseExpression();
                yield new Declaration.ConstDeclaration(constName, expression);
            }
            case VAR -> {
                shift(); // VAR
                String varName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // COLON
                Type varType = parseType();
                yield new Declaration.VarDeclaration(varName, varType);
            }
            case PROC -> {
                shift(); // PROC
                String procName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // LPAREN
                @SuppressWarnings("unchecked") List<Parameter> parameters =
                        (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseParamSeq();
                shift(); // RPAREN
                shift(); // IS
                Statement statement = parseStmt();
                yield new Declaration.ProcDeclaration(procName, parameters, statement);
            }
            case FUNC -> {
                shift(); // FUNC
                String funcName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // LPAREN
                @SuppressWarnings("unchecked") List<Parameter> parameters =
                        (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseParamSeq();
                shift(); // RPAREN
                shift(); // COLON
                Type type = parseType();
                shift(); // IS
                Expression expression = parseExpression();
                yield new Declaration.FuncDeclaration(funcName, parameters, type, expression);
            }
            case TYPE -> {
                shift(); // TYPE
                String typeName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // IS
                Type type = parseType();
                yield new Declaration.TypeDeclaration(typeName, type);
            }
            default -> throw new RuntimeException();
        };
    }

    private List<Parameter> parseParamSeq() throws IOException, SyntaxError {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parseParam());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(); // COMMA
                parameters.add(parseParam());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return parameters;
    }

    private Parameter parseParam() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case IDENTIFIER -> {
                String varName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // COLON
                Type varType = parseType();
                yield new VarParameter(varName, varType);
            }
            case VAR -> {
                shift(); // VAR
                String varName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // COLON
                Type varType = parseType();
                yield new VarParameter(varName, varType);
            }
            case PROC -> {
                shift(); // PROC
                String procName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // LPAREN
                @SuppressWarnings("unchecked") List<Parameter> parameters =
                        (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseParamSeq();
                shift(); // RPAREN
                yield new CallableParameter(procName, parameters, new VoidType());
            }
            case FUNC -> {
                shift(); // FUNC
                String funcName = ((TextToken) lastToken).getText();
                shift(); // IDENTIFIER
                shift(); // LPAREN
                @SuppressWarnings("unchecked") List<Parameter> parameters =
                        (lastToken.getKind() == Token.Kind.RPAREN) ? Collections.EMPTY_LIST : parseParamSeq();
                shift(); // RPAREN
                shift(); // COLON
                Type funcType = parseType();
                yield new CallableParameter(funcName, parameters, funcType);
            }
            default -> throw new RuntimeException();
        };
    }

    private void shift() throws IOException {
        lastToken = lexer.nextToken();
    }

}
