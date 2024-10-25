/*
 * @(#)Parser.java
 *
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

import triangle.ErrorReporter;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ActualParameter;
import triangle.abstractSyntaxTrees.actuals.ActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.ArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.RecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.Command;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.Declaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FormalParameter;
import triangle.abstractSyntaxTrees.formals.FormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.FieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDenoter;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;
import triangle.abstractSyntaxTrees.vnames.Vname;

import java.io.IOException;

@SuppressWarnings("SwitchStatementWithTooFewBranches") public class Parser {

    private final Lexer         lexicalAnalyser;
    private final ErrorReporter   errorReporter;
    private       Token currentTextToken;
    private       SourcePosition  previousTokenPosition;

    public Parser(Lexer lexer, ErrorReporter reporter) {
        lexicalAnalyser = lexer;
        errorReporter = reporter;
        previousTokenPosition = new SourcePosition();
    }

    // accept checks whether the current token matches tokenExpected.
    // If so, fetches the next TextToken.Kind.
    // If not, reports a syntactic error.

    public Program parseProgram() throws IOException, SyntaxError {
        previousTokenPosition.setStart(0);
        previousTokenPosition.setFinish(0);
        currentTextToken = lexicalAnalyser.nextToken();

        Command cAST = parseCommand();
        Program programAST = new Program(cAST, previousTokenPosition);
        if (currentTextToken.getKind() != Token.Kind.EOT) {
            syntacticError("\"%\" not expected after end of program");
        }
        return programAST;
    }

    // acceptIt simply moves to the next token with no checking
    // (used where we've already done the check)

    void accept(Token.Kind tokenExpected) throws SyntaxError, IOException {
        if (currentTextToken.getKind() == tokenExpected) {
            previousTokenPosition = currentTextToken.getPosition();
            currentTextToken = lexicalAnalyser.nextToken();
        } else {
            syntacticError(tokenExpected + " expected here");
        }
    }

    // start records the position of the start of a phrase.
    // This is defined to be the position of the first
    // character of the first token of the phrase.

    void acceptIt() throws IOException {
        previousTokenPosition = currentTextToken.getPosition();
        currentTextToken = lexicalAnalyser.nextToken();
    }

    // finish records the position of the end of a phrase.
    // This is defined to be the position of the last
    // character of the last token of the phrase.

    void start(SourcePosition position) {
        position.setStart(currentTextToken.getPosition().getStart());
    }

    void finish(SourcePosition position) {
        position.setFinish(previousTokenPosition.getFinish());
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // PROGRAMS
    //
    ///////////////////////////////////////////////////////////////////////////////

    void syntacticError(String message) throws SyntaxError {
        throw new SyntaxError(message);
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // LITERALS
    //
    ///////////////////////////////////////////////////////////////////////////////

    // parseIntegerLiteral parses an integer-literal, and constructs
    // a leaf AST to represent it.

    IntegerLiteral parseIntegerLiteral() throws SyntaxError, IOException {
        if (currentTextToken.getKind() == Token.Kind.INTLITERAL) {
            previousTokenPosition = currentTextToken.getPosition();
            IntegerLiteral IL = new IntegerLiteral(((TextToken) currentTextToken).getText(), previousTokenPosition);

            currentTextToken = lexicalAnalyser.nextToken();

            return IL;
        } else {
            syntacticError("integer literal expected here");
            return null;
        }
    }

    // parseCharacterLiteral parses a character-literal, and constructs a leaf
    // AST to represent it.

    CharacterLiteral parseCharacterLiteral() throws SyntaxError, IOException {
        if (currentTextToken.getKind() == Token.Kind.CHARLITERAL) {
            previousTokenPosition = currentTextToken.getPosition();
            CharacterLiteral CL = new CharacterLiteral(((TextToken) currentTextToken).getText(), previousTokenPosition);
            currentTextToken = lexicalAnalyser.nextToken();
            return CL;
        } else {
            syntacticError("character literal expected here");
            return null;
        }
    }

    // parseIdentifier parses an identifier, and constructs a leaf AST to
    // represent it.

    Identifier parseIdentifier() throws SyntaxError, IOException {

        if (currentTextToken.getKind() == Token.Kind.IDENTIFIER) {
            previousTokenPosition = currentTextToken.getPosition();
            Identifier I = new Identifier(((TextToken) currentTextToken).getText(), previousTokenPosition);
            currentTextToken = lexicalAnalyser.nextToken();

            return I;
        } else {
            syntacticError("identifier expected here");
            return null;
        }
    }

    // parseOperator parses an operator, and constructs a leaf AST to
    // represent it.

    Operator parseOperator() throws SyntaxError, IOException {
        if (currentTextToken.getKind() == Token.Kind.OPERATOR) {
            previousTokenPosition = currentTextToken.getPosition();
            Operator O = new Operator(((TextToken) currentTextToken).getText(), previousTokenPosition);
            currentTextToken = lexicalAnalyser.nextToken();
            return O;
        } else {
            syntacticError("operator expected here");
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // COMMANDS
    //
    ///////////////////////////////////////////////////////////////////////////////

    // parseCommand parses the command, and constructs an AST
    // to represent its phrase structure.

    Command parseCommand() throws SyntaxError, IOException {
        SourcePosition commandPos = new SourcePosition();

        start(commandPos);
        Command commandAST = parseSingleCommand(); // in case there's a syntactic error
        while (currentTextToken.getKind() == Token.Kind.SEMICOLON) {
            acceptIt();
            Command c2AST = parseSingleCommand();
            finish(commandPos);
            commandAST = new SequentialCommand(commandAST, c2AST, commandPos);
        }
        return commandAST;
    }

    Command parseSingleCommand() throws SyntaxError, IOException {
        SourcePosition commandPos = new SourcePosition();
        start(commandPos);

        return switch (currentTextToken.getKind()) {

            case IDENTIFIER -> {
                Identifier iAST = parseIdentifier();
                if (currentTextToken.getKind() == Token.Kind.LPAREN) {
                    acceptIt();
                    ActualParameterSequence apsAST = parseActualParameterSequence();
                    accept(Token.Kind.RPAREN);
                    finish(commandPos);
                    yield new CallCommand(iAST, apsAST, commandPos);
                } else {

                    Vname vAST = parseRestOfVname(iAST);
                    accept(Token.Kind.BECOMES);
                    Expression eAST = parseExpression();
                    finish(commandPos);
                    yield new AssignCommand(vAST, eAST, commandPos);
                }
            }

            case BEGIN -> {
                acceptIt();
                Command x = parseCommand();
                accept(Token.Kind.END);
                yield x;
            }

            case LET -> {
                acceptIt();
                Declaration dAST = parseDeclaration();
                accept(Token.Kind.IN);
                Command cAST = parseSingleCommand();
                finish(commandPos);
                yield new LetCommand(dAST, cAST, commandPos);
            }

            case IF -> {
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.Kind.THEN);
                Command c1AST = parseSingleCommand();
                accept(Token.Kind.ELSE);
                Command c2AST = parseSingleCommand();
                finish(commandPos);
                yield new IfCommand(eAST, c1AST, c2AST, commandPos);
            }

            case WHILE -> {
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.Kind.DO);
                Command cAST = parseSingleCommand();
                finish(commandPos);
                yield new WhileCommand(eAST, cAST, commandPos);
            }

            case SEMICOLON, END, ELSE, IN, EOT -> {
                finish(commandPos);
                yield new EmptyCommand(commandPos);
            }

            default -> {
                syntacticError("\"%\" cannot start a command");
                yield null;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // EXPRESSIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    Expression parseExpression() throws SyntaxError, IOException {
        SourcePosition expressionPos = new SourcePosition();

        start(expressionPos);

        return switch (currentTextToken.getKind()) {
            case LET -> {
                acceptIt();
                Declaration dAST = parseDeclaration();
                accept(Token.Kind.IN);
                Expression eAST = parseExpression();
                finish(expressionPos);
                yield new LetExpression(dAST, eAST, expressionPos);
            }
            case IF -> {
                acceptIt();
                Expression e1AST = parseExpression();
                accept(Token.Kind.THEN);
                Expression e2AST = parseExpression();
                accept(Token.Kind.ELSE);
                Expression e3AST = parseExpression();
                finish(expressionPos);
                yield new IfExpression(e1AST, e2AST, e3AST, expressionPos);
            }
            default -> parseSecondaryExpression();
        };
    }

    Expression parseSecondaryExpression() throws SyntaxError, IOException {
        SourcePosition expressionPos = new SourcePosition();
        start(expressionPos);

        Expression expressionAST = parsePrimaryExpression(); // in case there's a syntactic error
        while (currentTextToken.getKind() == Token.Kind.OPERATOR) {
            Operator opAST = parseOperator();
            Expression e2AST = parsePrimaryExpression();
            expressionAST = new BinaryExpression(expressionAST, opAST, e2AST, expressionPos);
        }
        return expressionAST;
    }

    Expression parsePrimaryExpression() throws SyntaxError, IOException {
        SourcePosition expressionPos = new SourcePosition();
        start(expressionPos);

        return switch (currentTextToken.getKind()) {

            case INTLITERAL -> {
                IntegerLiteral ilAST = parseIntegerLiteral();
                finish(expressionPos);
                yield new IntegerExpression(ilAST, expressionPos);
            }

            case CHARLITERAL -> {
                CharacterLiteral clAST = parseCharacterLiteral();
                finish(expressionPos);
                yield new CharacterExpression(clAST, expressionPos);
            }

            case LBRACK -> {
                acceptIt();
                ArrayAggregate aaAST = parseArrayAggregate();
                accept(Token.Kind.RBRACK);
                finish(expressionPos);
                yield new ArrayExpression(aaAST, expressionPos);
            }

            case LBRACE -> {
                acceptIt();
                RecordAggregate raAST = parseRecordAggregate();
                accept(Token.Kind.RBRACE);
                finish(expressionPos);
                yield new RecordExpression(raAST, expressionPos);
            }

            case IDENTIFIER -> {
                Identifier iAST = parseIdentifier();
                if (currentTextToken.getKind() == Token.Kind.LPAREN) {
                    acceptIt();
                    ActualParameterSequence apsAST = parseActualParameterSequence();
                    accept(Token.Kind.RPAREN);
                    finish(expressionPos);
                    yield new CallExpression(iAST, apsAST, expressionPos);
                } else {
                    Vname vAST = parseRestOfVname(iAST);
                    finish(expressionPos);
                    yield new VnameExpression(vAST, expressionPos);
                }
            }

            case OPERATOR -> {
                Operator opAST = parseOperator();
                Expression eAST = parsePrimaryExpression();
                finish(expressionPos);
                yield new UnaryExpression(opAST, eAST, expressionPos);
            }

            case LPAREN -> {
                acceptIt();
                Expression x = parseExpression();
                accept(Token.Kind.RPAREN);
                yield x;
            }

            default -> {
                syntacticError("\"%\" cannot start an expression");
                yield null;
            }
        };
    }

    RecordAggregate parseRecordAggregate() throws SyntaxError, IOException {
        SourcePosition aggregatePos = new SourcePosition();
        start(aggregatePos);

        Identifier iAST = parseIdentifier();
        accept(Token.Kind.IS);
        Expression eAST = parseExpression();

        return switch (currentTextToken.getKind()) {
            case COMMA -> {
                acceptIt();
                RecordAggregate aAST = parseRecordAggregate();
                finish(aggregatePos);
                yield new MultipleRecordAggregate(iAST, eAST, aAST, aggregatePos);
            }
            default -> {
                finish(aggregatePos);
                yield new SingleRecordAggregate(iAST, eAST, aggregatePos);
            }
        };
    }

    ArrayAggregate parseArrayAggregate() throws SyntaxError, IOException {
        SourcePosition aggregatePos = new SourcePosition();
        start(aggregatePos);

        Expression eAST = parseExpression();

        return switch (currentTextToken.getKind()) {
            case COMMA -> {
                acceptIt();
                ArrayAggregate aAST = parseArrayAggregate();
                finish(aggregatePos);
                yield new MultipleArrayAggregate(eAST, aAST, aggregatePos);
            }
            default -> {
                finish(aggregatePos);
                yield new SingleArrayAggregate(eAST, aggregatePos);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // VALUE-OR-VARIABLE NAMES
    //
    ///////////////////////////////////////////////////////////////////////////////

    Vname parseVname() throws SyntaxError, IOException {
        Identifier iAST = parseIdentifier();
        return parseRestOfVname(iAST);
    }

    Vname parseRestOfVname(Identifier identifierAST) throws SyntaxError, IOException {
        SourcePosition vnamePos = identifierAST.getPosition();
        Vname vAST = new SimpleVname(identifierAST, vnamePos);

        while (currentTextToken.getKind() == Token.Kind.DOT || currentTextToken.getKind() == Token.Kind.LBRACK) {

            if (currentTextToken.getKind() == Token.Kind.DOT) {
                acceptIt();
                Identifier iAST = parseIdentifier();
                vAST = new DotVname(vAST, iAST, vnamePos);
            } else {
                acceptIt();
                Expression eAST = parseExpression();
                accept(Token.Kind.RBRACK);
                finish(vnamePos);
                vAST = new SubscriptVname(vAST, eAST, vnamePos);
            }
        }
        return vAST;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // DECLARATIONS
    //
    ///////////////////////////////////////////////////////////////////////////////

    Declaration parseDeclaration() throws SyntaxError, IOException {
        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);
        Declaration declarationAST = parseSingleDeclaration();
        while (currentTextToken.getKind() == Token.Kind.SEMICOLON) {
            acceptIt();
            Declaration d2AST = parseSingleDeclaration();
            finish(declarationPos);
            declarationAST = new SequentialDeclaration(declarationAST, d2AST, declarationPos);
        }
        return declarationAST;
    }

    Declaration parseSingleDeclaration() throws SyntaxError, IOException {
        SourcePosition declarationPos = new SourcePosition();
        start(declarationPos);

        return switch (currentTextToken.getKind()) {

            case CONST -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.IS);
                Expression eAST = parseExpression();
                finish(declarationPos);
                yield new ConstDeclaration(iAST, eAST, declarationPos);
            }

            case VAR -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(declarationPos);
                yield new VarDeclaration(iAST, tAST, declarationPos);
            }

            case PROC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.Kind.RPAREN);
                accept(Token.Kind.IS);
                Command cAST = parseSingleCommand();
                finish(declarationPos);
                yield new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
            }

            case FUNC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.Kind.RPAREN);
                accept(Token.Kind.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                accept(Token.Kind.IS);
                Expression eAST = parseExpression();
                finish(declarationPos);
                yield new FuncDeclaration(iAST, fpsAST, tAST, eAST, declarationPos);
            }

            case TYPE -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.IS);
                TypeDenoter tAST = parseTypeDenoter();
                finish(declarationPos);
                yield new TypeDeclaration(iAST, tAST, declarationPos);
            }

            default -> {
                syntacticError("\"%\" cannot start a declaration");
                yield null;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // PARAMETERS
    //
    ///////////////////////////////////////////////////////////////////////////////

    FormalParameterSequence parseFormalParameterSequence() throws SyntaxError, IOException {
        FormalParameterSequence formalsAST;

        SourcePosition formalsPos = new SourcePosition();

        start(formalsPos);
        if (currentTextToken.getKind() == Token.Kind.RPAREN) {
            finish(formalsPos);
            formalsAST = new EmptyFormalParameterSequence(formalsPos);
        } else {
            formalsAST = parseProperFormalParameterSequence();
        }
        return formalsAST;
    }

    FormalParameterSequence parseProperFormalParameterSequence() throws SyntaxError, IOException {
        SourcePosition formalsPos = new SourcePosition();
        start(formalsPos);
        FormalParameter fpAST = parseFormalParameter();

        return switch (currentTextToken.getKind()) {
            case COMMA -> {
                acceptIt();
                FormalParameterSequence fpsAST = parseProperFormalParameterSequence();
                finish(formalsPos);
                yield new MultipleFormalParameterSequence(fpAST, fpsAST, formalsPos);
            }
            default -> {
                finish(formalsPos);
                yield new SingleFormalParameterSequence(fpAST, formalsPos);
            }
        };
    }

    FormalParameter parseFormalParameter() throws SyntaxError, IOException {
        SourcePosition formalPos = new SourcePosition();
        start(formalPos);

        return switch (currentTextToken.getKind()) {

            case IDENTIFIER -> {
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                yield new ConstFormalParameter(iAST, tAST, formalPos);
            }

            case VAR -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                yield new VarFormalParameter(iAST, tAST, formalPos);
            }

            case PROC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.Kind.RPAREN);
                finish(formalPos);
                yield new ProcFormalParameter(iAST, fpsAST, formalPos);
            }

            case FUNC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                accept(Token.Kind.LPAREN);
                FormalParameterSequence fpsAST = parseFormalParameterSequence();
                accept(Token.Kind.RPAREN);
                accept(Token.Kind.COLON);
                TypeDenoter tAST = parseTypeDenoter();
                finish(formalPos);
                yield new FuncFormalParameter(iAST, fpsAST, tAST, formalPos);
            }

            default -> {
                syntacticError("\"%\" cannot start a formal parameter");
                yield null;
            }
        };
    }

    ActualParameterSequence parseActualParameterSequence() throws SyntaxError, IOException {
        ActualParameterSequence actualsAST;

        SourcePosition actualsPos = new SourcePosition();

        start(actualsPos);
        if (currentTextToken.getKind() == Token.Kind.RPAREN) {
            finish(actualsPos);
            actualsAST = new EmptyActualParameterSequence(actualsPos);
        } else {
            actualsAST = parseProperActualParameterSequence();
        }
        return actualsAST;
    }

    ActualParameterSequence parseProperActualParameterSequence() throws SyntaxError, IOException {
        SourcePosition actualsPos = new SourcePosition();

        start(actualsPos);
        ActualParameter apAST = parseActualParameter();

        return switch (currentTextToken.getKind()) {
            case COMMA -> {
                acceptIt();
                ActualParameterSequence apsAST = parseProperActualParameterSequence();
                finish(actualsPos);
                yield new MultipleActualParameterSequence(apAST, apsAST, actualsPos);
            }
            default -> {
                finish(actualsPos);
                yield new SingleActualParameterSequence(apAST, actualsPos);
            }
        };
    }

    ActualParameter parseActualParameter() throws SyntaxError, IOException {
        SourcePosition actualPos = new SourcePosition();

        start(actualPos);

        return switch (currentTextToken.getKind()) {

            case IDENTIFIER, INTLITERAL, CHARLITERAL, OPERATOR, LET, IF, LPAREN, LBRACK, LBRACE -> {
                Expression eAST = parseExpression();
                finish(actualPos);
                yield new ConstActualParameter(eAST, actualPos);
            }

            case VAR -> {
                acceptIt();
                Vname vAST = parseVname();
                finish(actualPos);
                yield new VarActualParameter(vAST, actualPos);
            }

            case PROC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                finish(actualPos);
                yield new ProcActualParameter(iAST, actualPos);
            }

            case FUNC -> {
                acceptIt();
                Identifier iAST = parseIdentifier();
                finish(actualPos);
                yield new FuncActualParameter(iAST, actualPos);
            }

            default -> {
                syntacticError("\"%\" cannot start an actual parameter");
                yield null;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // TYPE-DENOTERS
    //
    ///////////////////////////////////////////////////////////////////////////////

    TypeDenoter parseTypeDenoter() throws SyntaxError, IOException {
        SourcePosition typePos = new SourcePosition();

        start(typePos);

        return switch (currentTextToken.getKind()) {
            case IDENTIFIER -> {
                Identifier iAST = parseIdentifier();
                finish(typePos);
                yield new SimpleTypeDenoter(iAST, typePos);
            }

            case ARRAY -> {
                acceptIt();
                IntegerLiteral ilAST = parseIntegerLiteral();
                accept(Token.Kind.OF);
                TypeDenoter tAST = parseTypeDenoter();
                finish(typePos);
                yield new ArrayTypeDenoter(ilAST, tAST, typePos);
            }

            case RECORD -> {
                acceptIt();
                FieldTypeDenoter fAST = parseFieldTypeDenoter();
                accept(Token.Kind.END);
                finish(typePos);
                yield new RecordTypeDenoter(fAST, typePos);
            }

            default -> {
                syntacticError("\"%\" cannot start a type denoter");
                yield null;
            }
        };
    }

    FieldTypeDenoter parseFieldTypeDenoter() throws SyntaxError, IOException {
        SourcePosition fieldPos = new SourcePosition();

        start(fieldPos);
        Identifier iAST = parseIdentifier();
        accept(Token.Kind.COLON);
        TypeDenoter tAST = parseTypeDenoter();

        return switch (currentTextToken.getKind()) {
            case COMMA -> {
                acceptIt();
                FieldTypeDenoter fAST = parseFieldTypeDenoter();
                finish(fieldPos);
                yield new MultipleFieldTypeDenoter(iAST, tAST, fAST, fieldPos);
            }
            default -> {
                finish(fieldPos);
                yield new SingleFieldTypeDenoter(iAST, tAST, fieldPos);
            }
        };
    }

}
