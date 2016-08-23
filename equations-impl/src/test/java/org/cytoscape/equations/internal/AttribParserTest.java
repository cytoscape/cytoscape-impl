package org.cytoscape.equations.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.equations.EquationParser;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Equations Impl (equations-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class AttribParserTest {
	
	private CyServiceRegistrar serviceRegistrar;
	private CyEventHelper eventHelper;
	private EquationParser parser;
	
	@Before
	public void init() {
		eventHelper = new DummyCyEventHelper();
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		
		parser = new EquationParserImpl(serviceRegistrar);
	}

	@Test
	public void testSimpleExpr() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Double.class);
		assertTrue(parser.parse("=42 - 12 + 3 * (4 - 2) + ${BOB:12}", attribNameToTypeMap));
	}

	@Test
	public void testStringVarDefault() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("STR", String.class);
		assertTrue(parser.parse("=${STR:\"xyz\"}", attribNameToTypeMap));
	}

	@Test
	public void testUnaryPlusAndMinus() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("attr1", Double.class);
		attribNameToTypeMap.put("attr2", Double.class);
		assertTrue(parser.parse("=-17.8E-14", attribNameToTypeMap));
		assertTrue(parser.parse("=+(${attr1} + ${attr2})", attribNameToTypeMap));
	}

	@Test
	public void testFunctionCall() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=42 + log(4 - 2)", attribNameToTypeMap));
	}

	@Test
	public void testExponentiation() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=2^3^4 - 0.0002", attribNameToTypeMap));
	}

	@Test
	public void testComparisons() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Double.class);
		attribNameToTypeMap.put("y", Double.class);
		attribNameToTypeMap.put("limit", Double.class);
		assertTrue(parser.parse("=${x} <= ${y}", attribNameToTypeMap));
		assertTrue(parser.parse("=-15.4^3 > ${limit}", attribNameToTypeMap));
	}

	@Test
	public void testVarargs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=LOG()", attribNameToTypeMap));
		assertTrue(parser.parse("=LOG(1)", attribNameToTypeMap));
		assertTrue(parser.parse("=LOG(1,2)", attribNameToTypeMap));
		assertFalse(parser.parse("=LOG(1,2,3)", attribNameToTypeMap));
	}

	@Test
	public void testFixedargs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=ABS()", attribNameToTypeMap));
		assertTrue(parser.parse("=ABS(1)", attribNameToTypeMap));
		assertFalse(parser.parse("=ABS(1,2)", attribNameToTypeMap));
	}

	@Test
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

	@Test
	public void testUPPERandLOWER() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=UPPER(\"Fred\")", attribNameToTypeMap));
		assertTrue(parser.parse("=\"bozo\"&LOWER(\"UPPER\")", attribNameToTypeMap));
	}

	@Test
	public void testBracelessAttribReferences() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Double.class);
		attribNameToTypeMap.put("FRED", Double.class);
		assertTrue(parser.parse("=$BOB+$FRED", attribNameToTypeMap));
	}

	@Test
	public void testIntegerToFloatingPointConversion() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Long.class);
		assertTrue(parser.parse("=$BOB > 5.3", attribNameToTypeMap));
	}

	@Test
	public void testMixedModeArithmetic() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Long.class);
		assertTrue(parser.parse("=$x + 2.0", attribNameToTypeMap));
	}
}
