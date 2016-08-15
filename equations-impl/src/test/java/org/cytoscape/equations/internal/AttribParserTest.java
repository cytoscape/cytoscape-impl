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


import java.util.HashMap;
import java.util.Map;

import junit.framework.*;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;


public class AttribParserTest extends TestCase {
	private final CyEventHelper eventHelper = new DummyCyEventHelper();
	private final EquationParser parser = new EquationParserImpl(eventHelper);

	public void testSimpleExpr() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Double.class);
		assertTrue(parser.parse("=42 - 12 + 3 * (4 - 2) + ${BOB:12}", attribNameToTypeMap));
	}

	public void testStringVarDefault() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("STR", String.class);
		assertTrue(parser.parse("=${STR:\"xyz\"}", attribNameToTypeMap));
	}

	public void testUnaryPlusAndMinus() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("attr1", Double.class);
		attribNameToTypeMap.put("attr2", Double.class);
		assertTrue(parser.parse("=-17.8E-14", attribNameToTypeMap));
		assertTrue(parser.parse("=+(${attr1} + ${attr2})", attribNameToTypeMap));
	}

	public void testFunctionCall() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=42 + log(4 - 2)", attribNameToTypeMap));
	}

	public void testExponentiation() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=2^3^4 - 0.0002", attribNameToTypeMap));
	}

	public void testComparisons() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Double.class);
		attribNameToTypeMap.put("y", Double.class);
		attribNameToTypeMap.put("limit", Double.class);
		assertTrue(parser.parse("=${x} <= ${y}", attribNameToTypeMap));
		assertTrue(parser.parse("=-15.4^3 > ${limit}", attribNameToTypeMap));
	}

	public void testVarargs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=LOG()", attribNameToTypeMap));
		assertTrue(parser.parse("=LOG(1)", attribNameToTypeMap));
		assertTrue(parser.parse("=LOG(1,2)", attribNameToTypeMap));
		assertFalse(parser.parse("=LOG(1,2,3)", attribNameToTypeMap));
	}

	public void testFixedargs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=ABS()", attribNameToTypeMap));
		assertTrue(parser.parse("=ABS(1)", attribNameToTypeMap));
		assertFalse(parser.parse("=ABS(1,2)", attribNameToTypeMap));
	}

	public void testNOT() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("logical", Boolean.class);
		assertFalse(parser.parse("=NOT()", attribNameToTypeMap));
		assertTrue(parser.parse("=NOT(true)", attribNameToTypeMap));
		assertTrue(parser.parse("=NOT(false)", attribNameToTypeMap));
		assertTrue(parser.parse("=NOT(3.2 < 12)", attribNameToTypeMap));
		assertTrue(parser.parse("=NOT(${logical})", attribNameToTypeMap));
		assertFalse(parser.parse("=NOT(true, true)", attribNameToTypeMap));
	}

	public void testUPPERandLOWER() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=UPPER(\"Fred\")", attribNameToTypeMap));
		assertTrue(parser.parse("=\"bozo\"&LOWER(\"UPPER\")", attribNameToTypeMap));
	}

	public void testBracelessAttribReferences() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Double.class);
		attribNameToTypeMap.put("FRED", Double.class);
		assertTrue(parser.parse("=$BOB+$FRED", attribNameToTypeMap));
	}

	public void testIntegerToFloatingPointConversion() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Long.class);
		assertTrue(parser.parse("=$BOB > 5.3", attribNameToTypeMap));
	}

	public void testMixedModeArithmetic() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Long.class);
		assertTrue(parser.parse("=$x + 2.0", attribNameToTypeMap));
	}
}
