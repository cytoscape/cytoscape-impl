/*
 Copyright (c) 2008, 2010, The Cytoscape Consortium (www.cytoscape.org)

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

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.model;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.equations.BooleanList;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.StringList;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.events.RowSetRecord;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CyTableTest extends AbstractCyTableTest {
	private final EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl());

	@Before
	public void setUp() {
		eventHelper = new DummyCyEventHelper();
		final Interpreter interpreter = new InterpreterImpl();
		table = new CyTableImpl("homer", "SUID", Long.class, true, true, SavePolicy.SESSION_FILE,
					eventHelper, interpreter);
		attrs = table.getRow(1L);
		table2 = new CyTableImpl("marge", "SUID", Long.class, true, true, SavePolicy.SESSION_FILE,
					 eventHelper, interpreter);
	}

	@After
	public void tearDown() {
		eventHelper = null;
		table = null;
		attrs = null;
	}

	@Test
	public void testSetEquation() {
		table.createColumn("someDouble", Double.class, false);
		table.createColumn("someOtherDouble", Double.class, false);

		compiler.compile("=6/3", new HashMap<String, Class<?>>());
		final Equation eqn = compiler.getEquation();
		attrs.set("someDouble", eqn);

		assertTrue(attrs.isSet("someDouble"));
		assertEquals(2.0, attrs.get("someDouble", Double.class).doubleValue(), 0.00001);
	}

	@Test
	public void testSetEquationWithIncompatibleEquationReturnType() {
		table.createColumn("someDouble", Double.class, false);
		table.createColumn("someOtherDouble", Double.class, false);

		compiler.compile("=\"String\"", new HashMap<String, Class<?>>());
		final Equation eqn = compiler.getEquation();
		try {
			attrs.set("someDouble", eqn);
			fail();
		} catch (IllegalArgumentException e) {
			/* Intentionally empty! */
		}
	}

	@Test
	public void testCreateList() {
		table.createListColumn("booleanList", Boolean.class, false);
		attrs.set("booleanList", new BooleanList());
		final BooleanList nonEmptyList = new BooleanList(true, false);
		attrs.set("booleanList", nonEmptyList);
		assertEquals(attrs.getList("booleanList", Boolean.class), nonEmptyList);
	}

	@Test
	public void testSetListWithACompatibleEquation() {
		table.createListColumn("stringList", String.class, false);
		attrs.set("stringList", new StringList());
		compiler.compile("=SLIST(\"one\",\"two\")", new HashMap<String, Class<?>>());
		final Equation eqn = compiler.getEquation();
		attrs.set("stringList", eqn);
		final StringList expectedList = new StringList("one", "two");
		assertEquals(attrs.getList("stringList", String.class), expectedList);
	}

	@Test
	public void testSetWithAnEvaluableCompatibleEquation() {
		table.createColumn("strings", String.class, false);
		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		compiler.compile("=\"one\"", new HashMap<String, Class<?>>());
		final Equation eqn = compiler.getEquation();
		attrs.set("strings", eqn);
		Object last = eventHelper.getLastPayload();
		assertNotNull(last);
		assertTrue(last instanceof RowSetRecord);
	}

	@Test
	public void testSetWithANonEvaluableCompatibleEquation() {
		table.createColumn("strings", String.class, false);
		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("a", String.class);
		compiler.compile("=$a&\"one\"", varnameToTypeMap);
		final Equation eqn = compiler.getEquation();
		attrs.set("strings", eqn);
		Object last = eventHelper.getLastPayload();
		assertNotNull(last);
		assertTrue(last instanceof RowSetRecord);
	}

	@Test
	public void testGetColumnValuesWithEquations() {
		table.createColumn("someLongs", Long.class, false);
		final CyRow row1 = table.getRow(1L);
		compiler.compile("=LEN(\"one\")", new HashMap<String, Class<?>>());
		final Equation eqn = compiler.getEquation();
		row1.set("someLongs", eqn);
		final CyRow row2 = table.getRow(2L);
		row2.set("someLongs", -27L);
		final List<Long> values = table.getColumn("someLongs").getValues(Long.class);
		assertTrue(values.size() == 2);
		assertTrue(values.contains(3L));
		assertTrue(values.contains(-27L));
	}

	@Test
	public void testGetLastInternalError() {
		assertNull(table.getLastInternalError());
		table.createColumn("someLongs", Long.class, false);
		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("someLongs", Long.class);
		compiler.compile("=$someLongs", varnameToTypeMap);
		attrs.set("someLongs", compiler.getEquation());
		assertNotNull(table.getLastInternalError());
	}

	@Test
	public void testGetColumnValuesWithEquationsWithDependentColumns() {
		table.createColumn("a", Double.class, false);
		table.createColumn("b", Double.class, false);
		attrs.set("a", 10.0);

		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("a", Double.class);
		compiler.compile("=$a+20", varnameToTypeMap);
		attrs.set("b", compiler.getEquation());
		Object last = eventHelper.getLastPayload();
		assertNotNull(last);
		assertTrue(last instanceof RowSetRecord);

		assertEquals(attrs.get("b", Double.class), 30.0, 0.00001);
	}

	@Test
	public void testSetWithAnEquationWhichReferencesANonExistentAttribute() {
		table.createColumn("a", Double.class, false);
		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("b", Double.class);
		compiler.compile("=$b+10", varnameToTypeMap);
		assertNull(table.getLastInternalError());
		attrs.set("a", compiler.getEquation());
		assertNotNull(table.getLastInternalError());
	}

	@Test
	public void testVirtualColumnWithAnEquationReference() {
		table.createColumn("x", Integer.class, false);
		table.createColumn("ss", String.class, false);
		CyRow row1 = table.getRow(1L);
		row1.set("x", 33);
		table2.createColumn("x2", Integer.class, false);
		CyRow row2 =  table2.getRow(1L);
		row2.set("x2", 33);
		table2.createColumn("s", String.class, true);
		table.addVirtualColumn("s1", "s", table2, "x2", "x", true);
		row2.set("s", "abc");

		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("s1", String.class);
		compiler.compile("=\"XXX\"&$s1", varnameToTypeMap);
		row1.set("ss", compiler.getEquation());

		assertEquals(row1.get("ss", String.class), "XXXabc");
	}
}
