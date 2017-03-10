package org.cytoscape.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.BooleanList;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.StringList;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

public class CyTableTest extends AbstractCyTableTest {
	
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
	private CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	private EquationCompiler compiler;

	
	@Before
	public void setUp() {
		eventHelper = new DummyCyEventHelper();
		compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));
		final Interpreter interpreter = new InterpreterImpl();
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		when(serviceRegistrar.getService(Interpreter.class)).thenReturn(interpreter);
		
		table = new CyTableImpl("homer", CyIdentifiable.SUID, Long.class, false, true, SavePolicy.SESSION_FILE,
					eventHelper, interpreter, 1000);
		attrs = table.getRow(1L);
		table2 = new CyTableImpl("marge", CyIdentifiable.SUID, Long.class, false, true, SavePolicy.SESSION_FILE,
					 eventHelper, interpreter, 1000);
		CyTableManagerImpl tblMgr = new CyTableManagerImpl(new CyNetworkTableManagerImpl(), 
				new CyNetworkManagerImpl(serviceRegistrar), serviceRegistrar);
		tblMgr.addTable(table);
		((CyTableImpl)table).handleEvent(new TableAddedEvent(tblMgr, table));
		tblMgr.addTable(table2);
		((CyTableImpl)table2).handleEvent(new TableAddedEvent(tblMgr, table2));

	}

	@After
	public void tearDown() {
		eventHelper = null;
		table = null;
		attrs = null;
	}

	@Test
	public void testPrimaryKeyValueIsSet() {
		Long pk = SUIDFactory.getNextSUID();
		CyRow row = table.getRow(pk);
		
		assertTrue(row.isSet(CyIdentifiable.SUID));
		assertEquals(pk, row.get(CyIdentifiable.SUID, Long.class));
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
			/* Intentionally empty. */
		}
	}

	@Test
	public void testCreateList() {
		table.createListColumn("booleanList", Boolean.class, false);
		attrs.set("booleanList", new BooleanList());
		final BooleanList nonEmptyList = new BooleanList(true, false);
		attrs.set("booleanList", nonEmptyList);
		assertListEquals(attrs.getList("booleanList", Boolean.class), nonEmptyList);
	}

	//@Test : TODO: We removed support for strongly typed lists so this test is
	//              now broken.
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
		table.createColumn("ss", String.class, false);
		CyRow row1 = table.getRow(1L);
		CyRow row2 =  table2.getRow(1L);
		table2.createColumn("s", String.class, true);
		table.addVirtualColumn("s1", "s", table2, table.getPrimaryKey().getName(), true);
		row2.set("s", "abc");

		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("s1", String.class);
		compiler.compile("=\"XXX\"&$s1", varnameToTypeMap);
		row1.set("ss", compiler.getEquation());

		assertEquals(row1.get("ss", String.class), "XXXabc");
		
		List<String> values = table.getColumn("ss").getValues(String.class);
		assertEquals(1, values.size());
		assertEquals("XXXabc", values.get(0));
	}
	
	@Test
	public void testColumnWithAnEquationReferenceToVirtualColumn() {
		table.createColumn("ss", String.class, false);
		CyRow row1 = table.getRow(1L);
		CyRow row2 =  table2.getRow(1L);
		table2.createColumn("s", String.class, true);
		table.addVirtualColumn("s1", "s", table2, table.getPrimaryKey().getName(), true);
		
		row1.set("ss", "abc");

		final Map<String, Class<?>> varnameToTypeMap = new HashMap<String, Class<?>>();
		varnameToTypeMap.put("ss", String.class);
		compiler.compile("=\"XXX\"&$ss", varnameToTypeMap);
		row2.set("s", compiler.getEquation());

		assertEquals("XXXabc", row1.get("s1", String.class));
		
		List<String> values = table.getColumn("s1").getValues(String.class);
		assertEquals(1, values.size());
		assertEquals("XXXabc", values.get(0));
	}
	
	
	@Test
	public void testVirtualColumnGetColumnValues() {
		table.createColumn("s", String.class, false);
		table.getRow(1L).set("s", "a");
		table.getRow(2L).set("s", "b");
		table.getRow(3L).set("s", "c");
		table.getRow(4L).set("s", "d");
		
		table2.addVirtualColumn("sv", "s", table, table2.getPrimaryKey().getName(), true);
		
		assertEquals(0, table2.getRowCount());
		table2.getRow(1L);
		table2.getRow(2L);
		assertEquals(2, table2.getRowCount());
		
		List<String> values = table2.getColumn("sv").getValues(String.class);
		assertEquals(values, Arrays.asList("a", "b"));
	}
	
	
	@Test
	public void testDefaultColumnValue() {
		table.createColumn("test", String.class, false, "foo");
		CyRow row1 = table.getRow(1L);
		assertEquals("foo", row1.get("test", String.class));
		assertEquals("foo", table.getColumn("test").getDefaultValue());
		
		table2.addVirtualColumn("virtualtest", "test", table, table.getPrimaryKey().getName(), true);
		assertEquals("foo", table2.getRow(1L).get("virtualtest", String.class));		
		assertEquals("foo", table2.getColumn("virtualtest").getDefaultValue());
	}
	
	@Test
	public void testVirtualColumnSet() {
		table.createColumn("real", String.class, false);
		table2.addVirtualColumn("virtual", "real", table, table.getPrimaryKey().getName(), true);
		CyRow row = table2.getRow(1L);
		row.set("virtual", "foo");
		for (Object payload : eventHelper.getAllPayloads()) {
			if (payload instanceof RowSetRecord) {
				RowSetRecord record = (RowSetRecord) payload;
				CyTable affectedTable = record.getRow().getTable();
				if (affectedTable == table) {
					assertEquals("real", record.getColumn());
				}
				if (affectedTable == table2) {
					assertEquals("virtual", record.getColumn());
				}
			}
		}
	}
}
