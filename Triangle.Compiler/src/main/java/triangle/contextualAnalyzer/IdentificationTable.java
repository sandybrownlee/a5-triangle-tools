/*
 * @(#)IdentificationTable.java               
 * 
 * Revisions and updates (c) 2022-2023 Sandy Brownlee. alexander.brownlee@stir.ac.uk
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

package triangle.contextualAnalyzer;

import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.types.*;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.Vname;

public final class IdentificationTable {

	private int level;
	private IdEntry latest;
	private Declaration invariant;

	public IdentificationTable() {
		level = 0;
		latest = null;
	}

	// Opens a new level in the identification table, 1 higher than the
	// current topmost level.
	public void openScope() {
		level++;
		//Reset invariant at beginning of new scope
		invariant = null;
	}

	// Closes the topmost level in the identification table, discarding
	// all entries belonging to that level.
	public void closeScope() {
		// Presumably, idTable.level > 0.
		var entry = this.latest;
		while (entry.level == this.level) {
			entry = entry.previous;
		}

		this.level--;
		this.latest = entry;

		//Clear invariant at the end of a scope
		invariant = null;
	}

	// Makes a new entry in the identification table for the given identifier
	// and attribute. The new entry belongs to the current level.
	// duplicated is set to to true iff there is already an entry for the
	// same identifier at the current level.
	public void enter(String id, Declaration attr) {
		attr.duplicated = retrieve(id, true) != null;
		this.latest = new IdEntry(id, attr, this.level, this.latest);

		//if attribute is invariant
		if (isInvariant(attr))
			//store it as invariant
			invariant = attr;
	}

	//Checks if Declaration attribute is an invariant
	//Returns true if invariant, otherwise returns false
	public boolean isInvariant(Declaration attr){
		if (attr instanceof ConstDeclaration) {
			//constants as always invariants
			return true;
		} else if (attr instanceof VarDeclaration || attr instanceof ProcDeclaration) {
			//never invariants
			return false;
		} else if (attr instanceof FuncDeclaration) {
			//parse attr to be of type FuncDeclaration
			FuncDeclaration funcDecl = (FuncDeclaration) attr;
			return isExpressionInvariant(funcDecl.E);
		} else if (attr instanceof UnaryOperatorDeclaration) {
			UnaryOperatorDeclaration uoDecl = (UnaryOperatorDeclaration) attr;
			return isTypeDenoterInvariant(uoDecl.ARG);
		} else if (attr instanceof BinaryOperatorDeclaration) {
			BinaryOperatorDeclaration boDecl = (BinaryOperatorDeclaration) attr;
			return isTypeDenoterInvariant(boDecl.ARG1) && isTypeDenoterInvariant(boDecl.ARG2);
		} else
			//Default - not invariant
			return false;
	}

	//Checks if TypeDenoter argument is an invariant
	//Returns true if invariant, otherwise returns false
	private boolean isTypeDenoterInvariant(TypeDenoter arg) {
		if(arg instanceof BoolTypeDenoter || arg instanceof CharTypeDenoter || arg instanceof IntTypeDenoter || arg instanceof SimpleTypeDenoter){
			//primitives are invariant
			return true;
		} else if (arg instanceof ArrayTypeDenoter) {
			ArrayTypeDenoter atd = (ArrayTypeDenoter) arg;
			return isTypeDenoterInvariant(atd.T);
		} else if (arg instanceof RecordTypeDenoter) {
			RecordTypeDenoter rtd = (RecordTypeDenoter) arg;
			return isFieldTypeDenoterInvariant(rtd.FT);
		} else
			return false;
	}

	//Checks if FieldTypeDenoter ft is an invariant
	//Returns true if invariant, otherwise returns false
	private boolean isFieldTypeDenoterInvariant(FieldTypeDenoter ft) {
		if (ft instanceof SingleFieldTypeDenoter){
			SingleFieldTypeDenoter sftd = (SingleFieldTypeDenoter) ft;
			return isTypeDenoterInvariant(sftd.T);
		} else if (ft instanceof MultipleFieldTypeDenoter) {
			MultipleFieldTypeDenoter mftd = (MultipleFieldTypeDenoter) ft;
			return isTypeDenoterInvariant(mftd.T) && isFieldTypeDenoterInvariant(mftd.FT);
		} else
			return false;
	}

	//Checks if Expression e is an invariant
	//Returns true if invariant, otherwise returns false
	private boolean isExpressionInvariant(Expression e) {
		if (e instanceof IntegerExpression || e instanceof CharacterExpression){
			//always invariant
			return true;
		} else if (e instanceof VnameExpression) {
			Vname vn = ((VnameExpression) e).V;
			return isVariableInvariant(vn);
		} else if (e instanceof UnaryExpression) {
			UnaryExpression ue = (UnaryExpression) e;
			return isExpressionInvariant(ue.E);
		} else if (e instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) e;
			return isExpressionInvariant(be.E1) && isExpressionInvariant(be.E2);
		} else
			return false;
	}

	//Checks if Vname vn is an invariant
	//Returns true if invariant, otherwise returns false
	private boolean isVariableInvariant(Vname vn) {
		if (vn instanceof SimpleVname){
			SimpleVname svn = (SimpleVname) vn;
			Declaration varDecl = retrieve(svn.I.spelling);
			if (varDecl instanceof ConstDeclaration){
				//always invariant
				return true;
			} else {
				//Check if declaration is only in current scope
				Declaration currScopeDecl = retrieve(svn.I.spelling, true);
				return currScopeDecl == varDecl;
			}
		} else
			return false;
	}

	// Finds an entry for the given identifier in the identification table,
	// if any. If there are several entries for that identifier, finds the
	// entry at the highest level, in accordance with the scope rules.
	// Returns null iff no entry is found.
	// otherwise returns the attribute field of the entry found.
	public Declaration retrieve(String id) {
		return retrieve(id, false);
	}

	// thisLevelOnly limits the search to only the current level
	public Declaration retrieve(String id, boolean thisLevelOnly) {
		var entry = this.latest;
		while (true) {
			if (entry == null || (thisLevelOnly && entry.level < this.level)) {
				break;
			} else if (entry.id.equals(id)) {
				//if attribute is invariant, return invariant
				if (invariant != null && isInvariant(entry.attr))
					return invariant;
				else
					return entry.attr;
			} else {
				entry = entry.previous;
			}
		}

		return null;
	}

	//read only - value stored for invariant
	public Declaration getInvariant(){
		return invariant;
	}

}
