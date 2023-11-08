/*
 * @(#)ObjectAddress.java                       
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

package triangle.codeGenerator.entities;

public class ObjectAddress {

	private final int level;

	private final int displacement;

	public ObjectAddress(int level, int displacement) {
		this.level = level;
		this.displacement = displacement;
	}

	public final int getLevel() {
		return level;
	}

	public int getDisplacement() {
		return displacement;
	}
}