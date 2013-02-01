package org.cytoscape.equations.internal.interpreter;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


public enum Instruction {
	FADD,    // addition of two floating-point numbers
	FSUB,    // subtraction of two floating-point numbers
	FMUL,    // multiplication of two floating-point numbers
	FDIV,    // division of two floating-point numbers
	FPOW,    // exponentiation of two floating-point numbers
	SCONCAT, // string concatentation
	BEQLF,   // equality test for two floating-point numbers
	BNEQLF,  // inequality test for two floating-point numbers
	BGTF,    // greater-than test for two floating-point numbers
	BLTF,    // less-than test for two floating-point numbers
	BGTEF,   // greater-than-or-equal test for two floating-point numbers
	BLTEF,   // less-than-or-equal test for two floating-point numbers
	BEQLS,   // equality test for strings
	BNEQLS,  // inequality test for strings
	BGTS,    // lexicographically greater-than test for strings
	BLTS,    // lexicographically less-than test for strings
	BGTES,   // lexicographically greater-than-or-equal test for strings
	BLTES,   // lexicographically less-than-or-equal test for strings
	BGTB,    // greater than test for booleans
	BLTB,    // less than test for booleans
	BGTEB,   // greater than or equal test for booleans
	BLTEB,   // less than or equal test for booleans
	BEQLB,   // equality test for booleans
	BNEQLB,  // inequality test for booleans
	CALL,    // function call
	FUMINUS, // unary minus for a floating-point numbers
	FUPLUS,  // unary plus for a floating-point numbers
	AREF,    // attribute reference
	AREF2,   // attribute reference with a default value
	FCONVI,  // conversion of an integer to floating point
	FCONVB,  // conversion of a boolean to floating point
	FCONVS,   // conversion of a string to floating point
	SCONVF,   // conversion of a floating point number to a string
	SCONVI,   // conversion of an integer to a string
	SCONVB    // conversion of a boolean to a string
}
