/*
  File: Instruction.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.equations.internal.interpreter;


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
