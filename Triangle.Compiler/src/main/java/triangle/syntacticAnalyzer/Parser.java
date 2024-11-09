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

// TODO: parse custom type definitions
public class Parser {

    private static final Set<Token.Kind> EXPRESSION_FIRST_SET = new HashSet<>();
    private static final Set<Token.Kind> STATEMENT_FIRST_SET = new HashSet<>();
    private static final Set<Token.Kind> DECLARATION_FIRST_SET = new HashSet<>();

    // manual transitive closure for FIRST sets of ident, expr, and stmt
    static {
        DECLARATION_FIRST_SET.addAll(Set.of(Token.Kind.CONST, Token.Kind.VAR, Token.Kind.PROC, Token.Kind.FUNC, Token.Kind.TYPE));

        EXPRESSION_FIRST_SET.addAll(
                Set.of(Token.Kind.INTLITERAL, Token.Kind.CHARLITERAL, Token.Kind.LBRACK, Token.Kind.LBRACE, Token.Kind.LPAREN,
                       Token.Kind.LET, Token.Kind.IF, Token.Kind.IDENTIFIER, Token.Kind.OPERATOR, Token.Kind.FALSE,
                       Token.Kind.TRUE
                ));

        STATEMENT_FIRST_SET.addAll(
                Set.of(Token.Kind.BEGIN, Token.Kind.LET, Token.Kind.IF, Token.Kind.WHILE, Token.Kind.LOOP, Token.Kind.REPEAT,
                       Token.Kind.IDENTIFIER
                ));
        STATEMENT_FIRST_SET.addAll(EXPRESSION_FIRST_SET);
    }

    private final Lexer lexer;
    private       Token lastToken;

    public Parser(Lexer lexer, Object ignored) {
        this.lexer = lexer;
    }

    public Statement parseProgram() throws IOException, SyntaxError {
        lastToken = lexer.nextToken();
        return parseStmt();
    }

    private List<Statement> parseStmtSeq() throws SyntaxError, IOException {
        List<Statement> statements = new ArrayList<>();
        statements.add(parseStmt());


        while (lastToken.getKind() == Token.Kind.SEMICOLON) {
            shift(Token.Kind.SEMICOLON);

            if (STATEMENT_FIRST_SET.contains(lastToken.getKind())) {
                statements.add(parseStmt());
            }
        }

        return statements;
    }

    private Statement parseStmt() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case BEGIN -> {
                SourcePosition start = shift(Token.Kind.BEGIN);
                List<Statement> statements = parseStmtSeq();
                shift(Token.Kind.END);
                yield new StatementBlock(start, statements);
            }
            case LET -> {
                SourcePosition start = shift(Token.Kind.LET);
                List<Declaration> declarations = parseDeclSeq();
                shift(Token.Kind.IN);
                Statement statement = parseStmt();
                yield new LetStatement(start, declarations, statement);
            }
            case IF -> {
                SourcePosition start = shift(Token.Kind.IF);
                Expression condition = parseExpression();
                shift(Token.Kind.THEN);

                Optional<Statement> consequent = (lastToken.getKind() == Token.Kind.ELSE) ? Optional.empty() : Optional.of(
                        parseStmt());
                shift(Token.Kind.ELSE);

                // else branches are allowed to end in SEMICOLON, another statement, or nothing at all
                Optional<Statement> alternative = switch (lastToken.getKind()) {
                    case SEMICOLON -> Optional.empty();
                    case Token.Kind k when STATEMENT_FIRST_SET.contains(k) -> Optional.of(parseStmt());
                    // anything that can't start a statement is assumed to be a skipped else branch
                    default -> Optional.empty();
                };

                yield new IfStatement(start, condition, consequent, alternative);
            }
            case WHILE -> {
                SourcePosition start = shift(Token.Kind.WHILE);
                Expression condition = parseExpression();
                shift(Token.Kind.DO);
                Statement statement = parseStmt();
                yield new WhileStatement(start, condition, statement);
            }
            case LOOP -> {
                SourcePosition start = shift(Token.Kind.LOOP);
                Statement statement1 = parseStmt();
                shift(Token.Kind.WHILE);
                Expression condition = parseExpression();
                shift(Token.Kind.DO);
                Statement statement2 = parseStmt();
                yield new LoopWhileStatement(start, condition, statement1, statement2);
            }
            case REPEAT -> {
                SourcePosition start = shift(Token.Kind.REPEAT);
                Statement statement = parseStmt();
                if (lastToken.getKind() == Token.Kind.WHILE) {
                    shift(Token.Kind.WHILE);
                    Expression condition = parseExpression();
                    yield new RepeatWhileStatement(start, condition, statement);
                } else {
                    shift(Token.Kind.UNTIL);
                    Expression condition = parseExpression();
                    yield new RepeatUntilStatement(start, condition, statement);
                }
            }
            case IDENTIFIER -> {
                Identifier identifier = parseIdentifier();
                if (lastToken.getKind() == Token.Kind.BECOMES) {
                    shift(Token.Kind.BECOMES);
                    Expression expression = parseExpression();
                    yield new AssignStatement(identifier.sourcePos(), identifier, expression);
                }

                // check if the identifier leads into a side-effectful operation
                if (lastToken.getKind() == Token.Kind.OPERATOR) {
                    String operatorString = ((TextToken) lastToken).getText();
                    SourcePosition start = shift(Token.Kind.OPERATOR);
                    Identifier.BasicIdentifier operator = new Identifier.BasicIdentifier(start, operatorString);

                    if (EXPRESSION_FIRST_SET.contains(lastToken.getKind())) {
                        Expression secondExpression = parseExpression();
                        yield new ExpressionStatement(start, new BinaryOp(start, operator, identifier, secondExpression));
                    }

                    yield new ExpressionStatement(start, new UnaryOp(start, operator, identifier));
                }

                Expression e = parseIfCall(identifier);
                yield new ExpressionStatement(e.sourcePos(), e);
            }
            case Token.Kind k when EXPRESSION_FIRST_SET.contains(k) -> {
                Expression e = parseExpression();
                yield new ExpressionStatement(e.sourcePos(), e);
            }
            default -> throw new SyntaxError(lastToken);
        };
    }

    private Expression parseExpression() throws IOException, SyntaxError {
        Expression firstExpression = switch (lastToken.getKind()) {
            case TRUE -> {
                SourcePosition start = shift(Token.Kind.TRUE);
                yield new LitBool(start, true);
            }
            case FALSE -> {
                SourcePosition start = shift(Token.Kind.FALSE);
                yield new LitBool(start, false);
            }
            case INTLITERAL -> {
                int value = Integer.parseInt(((TextToken) lastToken).getText());
                SourcePosition start = shift(Token.Kind.INTLITERAL);
                yield new LitInt(start, value);
            }
            case CHARLITERAL -> {
                char value = ((TextToken) lastToken).getText().charAt(0);
                SourcePosition start = shift(Token.Kind.CHARLITERAL);
                yield new LitChar(start, value);
            }
            case LBRACK -> {
                SourcePosition start = shift(Token.Kind.LBRACK);
                @SuppressWarnings("unchecked") List<Expression> arrayValues = (lastToken.getKind() == Token.Kind.RBRACK) ?
                        Collections.EMPTY_LIST : parseArraySeq();
                shift(Token.Kind.RBRACK);
                yield new LitArray(start, arrayValues);
            }
            case LBRACE -> {
                SourcePosition start = shift(Token.Kind.LBRACE);
                @SuppressWarnings("unchecked") List<LitRecord.RecordField> fields = (lastToken.getKind() == Token.Kind.RBRACE) ?
                        Collections.EMPTY_LIST : parseFieldSeq();
                shift(Token.Kind.RBRACE);
                yield new LitRecord(start, fields);
            }
            case LPAREN -> {
                shift(Token.Kind.LPAREN);
                Expression expression = parseExpression();
                shift(Token.Kind.RPAREN);
                yield expression;
            }
            case LET -> {
                SourcePosition start = shift(Token.Kind.LET);
                List<Declaration> declarations = parseDeclSeq();
                shift(Token.Kind.IN);
                Expression expression = parseExpression();
                yield new LetExpression(start, declarations, expression);
            }
            case IF -> {
                SourcePosition start = shift(Token.Kind.IF);
                Expression condition = parseExpression();
                shift(Token.Kind.THEN);
                Expression consequent = parseExpression();
                shift(Token.Kind.ELSE);
                Expression alternative = parseExpression();
                yield new IfExpression(start, condition, consequent, alternative);
            }
            case IDENTIFIER -> parseIfCall(parseIdentifier());
            // unary prefix op
            case OPERATOR -> {
                String operatorText = ((TextToken) lastToken).getText();
                SourcePosition start = shift(Token.Kind.OPERATOR);
                Identifier.BasicIdentifier operator = new Identifier.BasicIdentifier(start, operatorText);
                Expression expression = parseExpression();
                yield new UnaryOp(start, operator, expression);
            }

            default -> throw new SyntaxError(lastToken);
        };

        if (lastToken.getKind() == Token.Kind.OPERATOR) {
            String operatorText = ((TextToken) lastToken).getText();
            SourcePosition start = shift(Token.Kind.OPERATOR);
            Identifier.BasicIdentifier operator = new Identifier.BasicIdentifier(start, operatorText);

            if (EXPRESSION_FIRST_SET.contains(lastToken.getKind())) {
                Expression secondExpression = parseExpression();
                return new BinaryOp(start, operator, firstExpression, secondExpression);
            }

            return new UnaryOp(start, operator, firstExpression);
        }

        return firstExpression;
    }

    private Identifier parseIdentifier() throws IOException, SyntaxError {

        String identText = ((TextToken) lastToken).getText();
        SourcePosition start = shift(Token.Kind.IDENTIFIER);
        Identifier identifier = new Identifier.BasicIdentifier(start, identText);

        while (lastToken.getKind() == Token.Kind.DOT || lastToken.getKind() == Token.Kind.LBRACK) {
            if (lastToken.getKind() == Token.Kind.DOT) {
                shift(Token.Kind.DOT);
                Identifier recordField = parseIdentifier();
                identifier = new Identifier.RecordAccess(start, identifier, recordField);
            } else if (lastToken.getKind() == Token.Kind.LBRACK) {
                shift(Token.Kind.LBRACK);
                Expression arraySubscript = parseExpression();
                shift(Token.Kind.RBRACK);
                identifier = new Identifier.ArraySubscript(start, identifier, arraySubscript);
            }
        }

        return identifier;
    }

    private Expression parseIfCall(Identifier identifier) throws IOException, SyntaxError {
        if (lastToken.getKind() == Token.Kind.LPAREN) {
            if (identifier instanceof Identifier.BasicIdentifier basicIdentifier) {

                SourcePosition start = shift(Token.Kind.LPAREN);
                @SuppressWarnings("unchecked") List<Argument> arguments = (lastToken.getKind() == Token.Kind.RPAREN) ?
                        Collections.EMPTY_LIST : parseArgSeq();
                shift(Token.Kind.RPAREN);

                return new FunCall(start, basicIdentifier, arguments);
            }

            throw new RuntimeException("HOF is currently unsupported, so function call must be a simple vaariable name");
        }

        return identifier;
    }

    private Type parseType() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case IDENTIFIER -> {
                BasicType type = new BasicType(((TextToken) lastToken).getText());
                shift(Token.Kind.IDENTIFIER);
                yield type;
            }
            case ARRAY -> {
                shift(Token.Kind.ARRAY);
                int size = Integer.parseInt(((TextToken) lastToken).getText());
                shift(Token.Kind.INTLITERAL);
                shift(Token.Kind.OF);
                Type elementType = parseType();
                yield new ArrayType(size, elementType);
            }
            case RECORD -> {
                shift(Token.Kind.RECORD);
                @SuppressWarnings("unchecked") List<RecordType.FieldType> fieldTypes = (lastToken.getKind() == Token.Kind.END) ?
                        Collections.EMPTY_LIST : parseFieldTypeSeq();
                shift(Token.Kind.END);
                yield new RecordType(fieldTypes);
            }
            default -> throw new SyntaxError(lastToken);
        };
    }

    private List<Argument> parseArgSeq() throws IOException, SyntaxError {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(parseArg());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(Token.Kind.COMMA);
                arguments.add(parseArg());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return arguments;
    }

    private Argument parseArg() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case PROC, FUNC -> {
                SourcePosition start = shift(Token.Kind.PROC);
                Identifier callable = parseIdentifier();
                yield new Argument.FuncArgument(start, callable);
            }
            case VAR -> {
                SourcePosition start = shift(Token.Kind.VAR);
                yield new Argument.VarArgument(start, parseIdentifier());
            }
            case Token.Kind k when EXPRESSION_FIRST_SET.contains(k) -> parseExpression();
            default -> throw new SyntaxError(lastToken);
        };
    }

    private List<Expression> parseArraySeq() throws IOException, SyntaxError {
        List<Expression> arrayValues = new ArrayList<>();
        arrayValues.add(parseExpression());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(Token.Kind.COMMA);
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
                shift(Token.Kind.COMMA);
                recordFields.add(parseField());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return recordFields;
    }

    private LitRecord.RecordField parseField() throws IOException, SyntaxError {
        String fieldName = ((TextToken) lastToken).getText();
        shift(Token.Kind.IDENTIFIER);
        shift(Token.Kind.IS);
        return new LitRecord.RecordField(fieldName, parseExpression());
    }

    private List<RecordType.FieldType> parseFieldTypeSeq() throws IOException, SyntaxError {
        List<RecordType.FieldType> fieldTypes = new ArrayList<>();
        fieldTypes.add(parseFieldType());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(Token.Kind.COMMA);
                fieldTypes.add(parseFieldType());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return fieldTypes;
    }

    private RecordType.FieldType parseFieldType() throws IOException, SyntaxError {
        String fieldName = ((TextToken) lastToken).getText();
        shift(Token.Kind.IDENTIFIER);
        shift(Token.Kind.COLON);
        Type fieldType = parseType();
        return new RecordType.FieldType(fieldName, fieldType);
    }

    private List<Declaration> parseDeclSeq() throws IOException, SyntaxError {
        List<Declaration> declarations = new ArrayList<>();
        declarations.add(parseDecl());

        while (lastToken.getKind() == Token.Kind.SEMICOLON) {
            shift(Token.Kind.SEMICOLON);

            if (DECLARATION_FIRST_SET.contains(lastToken.getKind())) {
                declarations.add(parseDecl());
            }
        }

        return declarations;
    }

    private Declaration parseDecl() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case CONST -> {
                SourcePosition start = shift(Token.Kind.CONST);
                String constName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.IS);
                Expression expression = parseExpression();
                yield new Declaration.ConstDeclaration(start, constName, expression);
            }
            case VAR -> {
                SourcePosition start = shift(Token.Kind.VAR);
                String varName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.COLON);
                Type varType = parseType();
                yield new Declaration.VarDeclaration(start, varName, varType);
            }
            case PROC -> {
                SourcePosition start = shift(Token.Kind.PROC);
                String funcName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.LPAREN);
                @SuppressWarnings("unchecked") List<Parameter> parameters = (lastToken.getKind() == Token.Kind.RPAREN) ?
                        Collections.EMPTY_LIST : parseParamSeq();
                shift(Token.Kind.RPAREN);
                shift(Token.Kind.IS);
                Statement statement = parseStmt();
                yield new Declaration.ProcDeclaration(start, funcName, parameters, statement);
            }
            case FUNC -> {
                SourcePosition start = shift(Token.Kind.FUNC);
                String funcName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.LPAREN);
                @SuppressWarnings("unchecked") List<Parameter> parameters = (lastToken.getKind() == Token.Kind.RPAREN) ?
                        Collections.EMPTY_LIST : parseParamSeq();
                shift(Token.Kind.RPAREN);
                shift(Token.Kind.COLON);
                Type type = parseType();
                shift(Token.Kind.IS);
                Expression expression = parseExpression();
                yield new Declaration.FuncDeclaration(start, funcName, parameters, type, expression);
            }
            case TYPE -> {
                SourcePosition start = shift(Token.Kind.TYPE);
                String typeName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.IS);
                Type type = parseType();
                yield new Declaration.TypeDeclaration(start, typeName, type);
            }
            default -> throw new SyntaxError(lastToken);
        };
    }

    private List<Parameter> parseParamSeq() throws IOException, SyntaxError {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(parseParam());

        if (lastToken.getKind() == Token.Kind.COMMA) {
            do {
                shift(Token.Kind.COMMA);
                parameters.add(parseParam());
            } while (lastToken.getKind() == Token.Kind.COMMA);
        }

        return parameters;
    }

    private Parameter parseParam() throws IOException, SyntaxError {
        return switch (lastToken.getKind()) {
            case IDENTIFIER -> {
                String varName = ((TextToken) lastToken).getText();
                SourcePosition start = shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.COLON);
                Type varType = parseType();
                yield new ConstParameter(start, varName, varType);
            }
            case VAR -> {
                SourcePosition start = shift(Token.Kind.VAR);
                String varName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.COLON);
                Type varType = parseType();
                yield new VarParameter(start, varName, varType);
            }
            case PROC -> {
                SourcePosition start = shift(Token.Kind.PROC);
                String funcName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.LPAREN);
                @SuppressWarnings("unchecked") List<Parameter> parameters = (lastToken.getKind() == Token.Kind.RPAREN) ?
                        Collections.EMPTY_LIST : parseParamSeq();
                shift(Token.Kind.RPAREN);
                yield new FuncParameter(start, funcName, parameters, new Type.Void());
            }
            case FUNC -> {
                SourcePosition start = shift(Token.Kind.FUNC);
                String funcName = ((TextToken) lastToken).getText();
                shift(Token.Kind.IDENTIFIER);
                shift(Token.Kind.LPAREN);
                @SuppressWarnings("unchecked") List<Parameter> parameters = (lastToken.getKind() == Token.Kind.RPAREN) ?
                        Collections.EMPTY_LIST : parseParamSeq();
                shift(Token.Kind.RPAREN);
                shift(Token.Kind.COLON);
                Type funcType = parseType();
                yield new FuncParameter(start, funcName, parameters, funcType);
            }
            default -> throw new SyntaxError(lastToken);
        };
    }

    private SourcePosition shift(Token.Kind expectedKind) throws IOException, SyntaxError {
        if (lastToken.getKind() != expectedKind) {
            throw new SyntaxError(lastToken, expectedKind);
        }

        SourcePosition sourcePosition = new SourcePosition(lastToken.getLine(), lastToken.getColumn());
        lastToken = lexer.nextToken();

        return sourcePosition;
    }

}
