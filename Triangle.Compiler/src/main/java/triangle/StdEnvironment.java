/*
 * @(#)StdEnvironment.java                       
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

package triangle;

import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDenoter;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.syntacticAnalyzer.SourcePosition;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;

public final class StdEnvironment {

	// These are small ASTs representing standard types.

	public static TypeDenoter booleanType, charType, integerType, anyType, errorType;

	public static TypeDeclaration booleanDecl, charDecl, integerDecl;

	// These are small ASTs representing "declarations" of standard entities.

	public static ConstDeclaration falseDecl, trueDecl, maxintDecl;

	public static UnaryOperatorDeclaration notDecl;

	public static BinaryOperatorDeclaration andDecl, orDecl, addDecl, subtractDecl, multiplyDecl, divideDecl,
			moduloDecl, equalDecl, unequalDecl, lessDecl, notlessDecl, greaterDecl, notgreaterDecl, starStarDecl;

	public static ProcDeclaration getDecl, putDecl, getintDecl, putintDecl, geteolDecl, puteolDecl;

	public static FuncDeclaration chrDecl, ordDecl, eolDecl, eofDecl;



public static void initialize() {
    SourcePosition dummyPos = new SourcePosition();

    // Initialize basic types
    booleanType = new SimpleTypeDenoter(new Identifier("boolean", dummyPos), dummyPos);
    charType = new SimpleTypeDenoter(new Identifier("char", dummyPos), dummyPos);
    integerType = new SimpleTypeDenoter(new Identifier("integer", dummyPos), dummyPos);
    anyType = new SimpleTypeDenoter(new Identifier("any", dummyPos), dummyPos);
    errorType = new SimpleTypeDenoter(new Identifier("error", dummyPos), dummyPos);

    // Standard type declarations
    booleanDecl = new TypeDeclaration(new Identifier("boolean", dummyPos), booleanType, dummyPos);
    charDecl = new TypeDeclaration(new Identifier("char", dummyPos), charType, dummyPos);
    integerDecl = new TypeDeclaration(new Identifier("integer", dummyPos), integerType, dummyPos);

    // Constant declarations
    falseDecl = new ConstDeclaration(new Identifier("false", dummyPos), booleanType, dummyPos);
    trueDecl = new ConstDeclaration(new Identifier("true", dummyPos), booleanType, dummyPos);
    maxintDecl = new ConstDeclaration(new Identifier("maxint", dummyPos), integerType, dummyPos);

    // Unary operator declarations
    notDecl = new UnaryOperatorDeclaration(new Operator("\\", dummyPos), booleanType, booleanType, dummyPos);

    // Binary operator declarations
    andDecl = new BinaryOperatorDeclaration(new Operator("/\\", dummyPos), booleanType, booleanType, booleanType, dummyPos);
    orDecl = new BinaryOperatorDeclaration(new Operator("\\/", dummyPos), booleanType, booleanType, booleanType, dummyPos);
    addDecl = new BinaryOperatorDeclaration(new Operator("+", dummyPos), integerType, integerType, integerType, dummyPos);
    subtractDecl = new BinaryOperatorDeclaration(new Operator("-", dummyPos), integerType, integerType, integerType, dummyPos);
    multiplyDecl = new BinaryOperatorDeclaration(new Operator("*", dummyPos), integerType, integerType, integerType, dummyPos);
    divideDecl = new BinaryOperatorDeclaration(new Operator("/", dummyPos), integerType, integerType, integerType, dummyPos);
    moduloDecl = new BinaryOperatorDeclaration(new Operator("%", dummyPos), integerType, integerType, integerType, dummyPos);
    equalDecl = new BinaryOperatorDeclaration(new Operator("=", dummyPos), anyType, anyType, booleanType, dummyPos);
    unequalDecl = new BinaryOperatorDeclaration(new Operator("!=", dummyPos), anyType, anyType, booleanType, dummyPos);
    lessDecl = new BinaryOperatorDeclaration(new Operator("<", dummyPos), integerType, integerType, booleanType, dummyPos);
    notlessDecl = new BinaryOperatorDeclaration(new Operator(">=", dummyPos), integerType, integerType, booleanType, dummyPos);
    greaterDecl = new BinaryOperatorDeclaration(new Operator(">", dummyPos), integerType, integerType, booleanType, dummyPos);
    notgreaterDecl = new BinaryOperatorDeclaration(new Operator("<=", dummyPos), integerType, integerType, booleanType, dummyPos);
    starStarDecl = new BinaryOperatorDeclaration(new Operator("**", dummyPos), integerType, integerType, integerType, dummyPos);

    // Procedure declarations
    getDecl = new ProcDeclaration(
    	    new Identifier("get", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos), // No parameters
    	    new EmptyCommand(dummyPos),                 // No body
    	    dummyPos
    	);

    	putDecl = new ProcDeclaration(
    	    new Identifier("put", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos),
    	    new EmptyCommand(dummyPos),
    	    dummyPos
    	);

    	getintDecl = new ProcDeclaration(
    	    new Identifier("getint", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos),
    	    new EmptyCommand(dummyPos),
    	    dummyPos
    	);

    	putintDecl = new ProcDeclaration(
    	    new Identifier("putint", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos),
    	    new EmptyCommand(dummyPos),
    	    dummyPos
    	);

    	geteolDecl = new ProcDeclaration(
    	    new Identifier("geteol", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos),
    	    new EmptyCommand(dummyPos),
    	    dummyPos
    	);

    	puteolDecl = new ProcDeclaration(
    	    new Identifier("puteol", dummyPos),
    	    new EmptyFormalParameterSequence(dummyPos),
    	    new EmptyCommand(dummyPos),
    	    dummyPos
    	);

    // Function declarations
    	chrDecl = new FuncDeclaration(
    		    new Identifier("chr", dummyPos), // Function name
    		    new EmptyFormalParameterSequence(dummyPos), // No parameters
    		    integerType, // Input type (integer)
    		    new EmptyExpression(dummyPos), // Placeholder for the function body
    		    dummyPos
    		);

    		ordDecl = new FuncDeclaration(
    		    new Identifier("ord", dummyPos),
    		    new EmptyFormalParameterSequence(dummyPos),
    		    charType, // Input type (character)
    		    new EmptyExpression(dummyPos),
    		    dummyPos
    		);

    		eolDecl = new FuncDeclaration(
    		    new Identifier("eol", dummyPos),
    		    new EmptyFormalParameterSequence(dummyPos),
    		    booleanType, // Return type is boolean
    		    new EmptyExpression(dummyPos),
    		    dummyPos
    		);

    		eofDecl = new FuncDeclaration(
    		    new Identifier("eof", dummyPos),
    		    new EmptyFormalParameterSequence(dummyPos),
    		    booleanType, // Return type is boolean
    		    new EmptyExpression(dummyPos),
    		    dummyPos
    		);
}
}

