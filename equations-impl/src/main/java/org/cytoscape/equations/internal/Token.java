/*
  File: Token.java

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
package org.cytoscape.equations.internal;


public enum Token {
	//                     COMP. OP.    ARITH. OP.    STRING OP.
	STRING_CONSTANT("?",     false,       false,        false),
	FLOAT_CONSTANT("?",      false,       false,        false),
	BOOLEAN_CONSTANT("?",    false,       false,        false),
	IDENTIFIER("?",          false,       false,        false),
	OPEN_BRACE("{",          false,       false,        false),
	CLOSE_BRACE("}",         false,       false,        false),
	OPEN_PAREN("(",          false,       false,        false),
	CLOSE_PAREN(")",         false,       false,        false),
	COLON(":",               false,       false,        false),
	CARET("^",               false,       true,         false),
	PLUS("+",                false,       true,         false),
	MINUS("-",               false,       true,         false),
	DIV("/",                 false,       true,         false),
	MUL("*",                 false,       true,         false),
	EQUAL("=",               true,        false,        false),
	NOT_EQUAL("<>",          true,        false,        false),
	GREATER_THAN(">",        true,        false,        false),
	LESS_THAN("<",           true,        false,        false),
	GREATER_OR_EQUAL(">=",   true,        false,        false),
	LESS_OR_EQUAL("<=",      true,        false,        false),
	DOLLAR("$",              false,       false,        false),
	COMMA(",",               false,       false,        false),
	AMPERSAND("&",           false,       false,        true),
	EOS("?",                 false,       false,        false),
	ERROR("?",               false,       false,        false);
	

	private final String asString;
	private final boolean isComparisonOperator;
	private final boolean isArithmeticOperator;
	private final boolean isStringOperator;

	Token(final String asString, final boolean isComparisonOperator,
		    final boolean isArithmeticOperator, final boolean isStringOperator)
	{
		this.asString = asString;
		this.isComparisonOperator = isComparisonOperator;
		this.isArithmeticOperator = isArithmeticOperator;
		this.isStringOperator     = isStringOperator;
	}

	public String asString() { return asString; }
	public boolean isComparisonOperator() { return isComparisonOperator; }
	public boolean isArithmeticOperator() { return isArithmeticOperator; }
	public boolean isStringOperator() { return isStringOperator; }
}
