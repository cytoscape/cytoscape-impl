package org.cytoscape.equations.internal;

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
