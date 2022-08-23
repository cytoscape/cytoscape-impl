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
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	public void testEscapedIdents() throws Exception {
		// Need to escape colon, backslash and closing brace, because these have special meaning, but thats it.
		var types = new HashMap<String, Class<?>>();
		types.put("NS::name1 (1,2)", Long.class);
		types.put("NS::name2 \\ {1:2}", Long.class);
		assertTrue(parser.parse("=${NS::name1 (1,2)}", types));
		assertTrue(parser.parse("=${NS::name1 \\(1\\,2\\)}", types));
		assertTrue(parser.parse("=${NS::name2 \\\\ {1\\:2\\}}", types));
	}
	
	
	@Test
	public void testSimpleExpr() throws Exception {
		var types = Map.<String,Class<?>>of("BOB", Double.class);
		assertTrue(parser.parse("=42 - 12 + 3 * (4 - 2) + ${BOB:12}", types));
	}
	
	@Test
	public void testColumnStartsWithNumber() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("1BOB", Double.class);
		assertTrue(parser.parse("=${1BOB}", types));
	}
	@Test
	public void testColumnStartsWithNumberDefault() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("1BOB", Double.class);
		assertTrue(parser.parse("=${1BOB:12}", types));
	}

	@Test
	public void testStringVarDefault() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("STR", String.class);
		assertTrue(parser.parse("=${STR:\"xyz\"}", types));
	}

	@Test
	public void testUnaryPlusAndMinus() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("attr1", Double.class);
		types.put("attr2", Double.class);
		assertTrue(parser.parse("=-17.8E-14", types));
		assertTrue(parser.parse("=+(${attr1} + ${attr2})", types));
	}

	@Test
	public void testFunctionCall() throws Exception {
		var types = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=42 + log(4 - 2)", types));
	}

	@Test
	public void testExponentiation() throws Exception {
		var types = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=2^3^4 - 0.0002", types));
	}

	@Test
	public void testComparisons() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("x", Double.class);
		types.put("y", Double.class);
		types.put("limit", Double.class);
		assertTrue(parser.parse("=${x} <= ${y}", types));
		assertTrue(parser.parse("=-15.4^3 > ${limit}", types));
	}

	@Test
	public void testVarargs() throws Exception {
		var types = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=LOG()", types));
		assertTrue(parser.parse("=LOG(1)", types));
		assertTrue(parser.parse("=LOG(1,2)", types));
		assertFalse(parser.parse("=LOG(1,2,3)", types));
	}

	@Test
	public void testFixedargs() throws Exception {
		var types = new HashMap<String, Class<?>>();
		assertFalse(parser.parse("=ABS()", types));
		assertTrue(parser.parse("=ABS(1)", types));
		assertFalse(parser.parse("=ABS(1,2)", types));
	}

	@Test
	public void testNOT() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("logical", Boolean.class);
		assertFalse(parser.parse("=NOT()", types));
		assertTrue(parser.parse("=NOT(true)", types));
		assertTrue(parser.parse("=NOT(false)", types));
		assertTrue(parser.parse("=NOT(3.2 < 12)", types));
		assertTrue(parser.parse("=NOT(${logical})", types));
		assertFalse(parser.parse("=NOT(true, true)", types));
	}

	@Test
	public void testUPPERandLOWER() throws Exception {
		var types = new HashMap<String, Class<?>>();
		assertTrue(parser.parse("=UPPER(\"Fred\")", types));
		assertTrue(parser.parse("=\"bozo\"&LOWER(\"UPPER\")", types));
	}

	@Test
	public void testBracelessAttribReferences() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("BOB", Double.class);
		types.put("FRED", Double.class);
		assertTrue(parser.parse("=$BOB+$FRED", types));
	}

	@Test
	public void testIntegerToFloatingPointConversion() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("BOB", Long.class);
		assertTrue(parser.parse("=$BOB > 5.3", types));
	}

	@Test
	public void testMixedModeArithmetic() throws Exception {
		var types = new HashMap<String, Class<?>>();
		types.put("x", Long.class);
		assertTrue(parser.parse("=$x + 2.0", types));
	}
	
}
