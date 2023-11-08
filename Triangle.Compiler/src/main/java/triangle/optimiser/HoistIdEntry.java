/*
 * @(#)IdEntry.java                       
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

package triangle.optimiser;

import triangle.abstractSyntaxTrees.declarations.Declaration;

public class HoistIdEntry {

	protected String id;
	protected Declaration attr;
	protected int level;
	protected HoistIdEntry previous;
	protected Boolean mutated = false;

	HoistIdEntry(String id, Declaration attr, int level, HoistIdEntry previous) {
		this.id = id;
		this.attr = attr;
		this.level = level;
		this.previous = previous;
	}
	
	public void setMutated() {
		this.mutated = true;
	}
	
	public Boolean isMutated() {
		return this.mutated;
	}

}
