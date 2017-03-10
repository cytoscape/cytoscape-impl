package org.cytoscape.equations.internal.interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.equations.Function;
import org.cytoscape.equations.IdentDescriptor;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
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

public class InterpreterTest {
	
	static private class BadReturnFunction implements Function {
		public String getName() { return "BAD"; }
		public String getFunctionSummary() { return "Returns an invalid type at runtime."; }
		public String getUsageDescription() { return "Call this with \"BAD()\"."; }
		public Class<?> getReturnType() { return Double.class; }
		public Class<?> validateArgTypes(final Class<?>[] argTypes) { return argTypes.length == 0 ? Double.class : null; }
		public Object evaluateFunction(final Object[] args) { return new Integer(1); }
		public List<Class<?>> getPossibleArgTypes(final Class<?>[] leadingArgs) { return null; }
	}
	
	private CyServiceRegistrar serviceRegistrar;
	private CyEventHelper eventHelper;
	private EquationParserImpl parser;
	private EquationCompilerImpl compiler;
	private Interpreter interpreter;

	@Before
	public void init() {
		eventHelper = new DummyCyEventHelper();
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		
		parser = new EquationParserImpl(serviceRegistrar);
		compiler = new EquationCompilerImpl(parser);
		interpreter = new InterpreterImpl();
	}
	
	@Test
	public void testSimpleStringConcatExpr() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("s1", String.class);
		assertTrue(compiler.compile("=\"Fred\"&${s1}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("s1", new IdentDescriptor("Bob"));
		assertEquals("FredBob", interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testSimpleExpr() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Double.class);
		assertTrue(compiler.compile("=42 - 12 + 3 * (4 - 2) + ${BOB:12}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("BOB", new IdentDescriptor(-10.0));
		assertEquals(new Double(26.0), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testUnaryPlusAndMinus() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("attr1", Double.class);
		attribNameToTypeMap.put("attr2", Double.class);
		assertTrue(compiler.compile("=-17.8E-14", attribNameToTypeMap));
		assertTrue(compiler.compile("=+(${attr1} + ${attr2})", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("attr1", new IdentDescriptor(5.5));
		nameToDescriptorMap.put("attr2", new IdentDescriptor(6.5));
		assertEquals(new Double(12.0), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testBinaryMinus() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("attr1", Double.class);
		attribNameToTypeMap.put("attr2", Double.class);
		assertTrue(compiler.compile("=-17.8E-14", attribNameToTypeMap));
		assertTrue(compiler.compile("=1-1.5", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("attr1", new IdentDescriptor(5.5));
		nameToDescriptorMap.put("attr2", new IdentDescriptor(6.5));
		assertEquals(new Double(-0.5), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testUnaryPlusAndMinus2() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("attr1", Long.class);
		assertTrue(compiler.compile("=-$attr1", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("attr1", new IdentDescriptor(5L));
		assertEquals(new Double(-5.0), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testFunctionCall() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(compiler.compile("=42 + log(4 - 2)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		assertEquals(new Double(42.0 + Math.log10(4.0 - 2.0)),
			     interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testExponentiation() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(compiler.compile("=2^3^4 - 0.0002", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		assertEquals(new Double(Math.pow(2.0, Math.pow(3.0, 4.0)) - 0.0002),
			     interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testComparisons() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Double.class);
		attribNameToTypeMap.put("y", Double.class);
		attribNameToTypeMap.put("limit", Double.class);
		assertTrue(compiler.compile("=${x} <= ${y}", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(1.2));
		nameToDescriptorMap.put("y", new IdentDescriptor(-3.8e-12));
		nameToDescriptorMap.put("limit", new IdentDescriptor(-65.23e12));
		assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
		
		assertTrue(compiler.compile("=-15.4^3 > ${limit}", attribNameToTypeMap));
		assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testVarargs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(compiler.compile("=LOG()", attribNameToTypeMap));
		assertTrue(compiler.compile("=LOG(1)", attribNameToTypeMap));
		assertTrue(compiler.compile("=LOG(1,2)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		assertEquals(new Double(Math.log(1.0) / Math.log(2.0)),
			     interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
		assertFalse(compiler.compile("=LOG(1,2,3)", attribNameToTypeMap));
	}

	@Test
	public void testFixedArgs() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertFalse(compiler.compile("=ABS()", attribNameToTypeMap));
		assertTrue(compiler.compile("=ABS(-1.5e10)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		assertEquals(new Double(1.5e10), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
		assertFalse(compiler.compile("=ABS(1,2)", attribNameToTypeMap));
	}

	@Test
	public void testDEFINED() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Double.class);
		assertTrue(compiler.compile("=defined(x)", attribNameToTypeMap));
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(1.2));
		assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=DEFINED(${limit})", attribNameToTypeMap));
		assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testIntegerToFloatingPointConversion() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("BOB", Long.class);

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("BOB", new IdentDescriptor(new Long(3)));

		assertTrue(compiler.compile("=$BOB > 5.3", attribNameToTypeMap));
		assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=$BOB <= 5.3", attribNameToTypeMap));
		assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testMixedModeArithmetic() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		attribNameToTypeMap.put("x", Long.class);

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		nameToDescriptorMap.put("x", new IdentDescriptor(new Long(3)));

		assertTrue(compiler.compile("=$x + 2.0", attribNameToTypeMap));
		assertEquals(new Double(5.0), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=TRUE + TRUE", attribNameToTypeMap));
		assertEquals(new Double(2.0), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}

	@Test
	public void testFunctionWithBadRuntimeReturnType() throws Exception {
		final Function badReturnFunction = new BadReturnFunction();
		if (parser.getFunction(badReturnFunction.getName()) == null) // Avoid duplicate registration!
			parser.registerFunctionInternal(badReturnFunction);

		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		assertTrue(compiler.compile("=BAD()", attribNameToTypeMap));

		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();
		try {
			interpreter.execute(compiler.getEquation(), nameToDescriptorMap);
		} catch (final IllegalStateException e) {
			// If we get here, everything is as expected and we let the test pass!
		}
	}

	@Test
	public void testComparisonsWithBooleans() throws Exception {
		final Map<String, Class<?>> attribNameToTypeMap = new HashMap<String, Class<?>>();
		final Map<String, IdentDescriptor> nameToDescriptorMap = new HashMap<String, IdentDescriptor>();

		assertTrue(compiler.compile("=TRUE < FALSE", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=FALSE < TRUE", attribNameToTypeMap));
                assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=\"a\" < TRUE", attribNameToTypeMap));
                assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=\"ZYX\" < FALSE", attribNameToTypeMap));
                assertEquals(new Boolean(true), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=\"a\" > TRUE", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=\"ZYX\" > FALSE", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=TRUE < \"a\"", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=FALSE < \"ZYX\"", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=TRUE < 0", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));

		assertTrue(compiler.compile("=FALSE < -1", attribNameToTypeMap));
                assertEquals(new Boolean(false), interpreter.execute(compiler.getEquation(), nameToDescriptorMap));
	}
}
