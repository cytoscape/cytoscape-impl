package org.cytoscape.model;

import static org.junit.Assert.*;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.junit.Before;
import org.junit.Test;

public class EquationTest {
	private TableTestSupport support;
	private EquationParserImpl parser;

	@Before
	public void setUp() {
		support = new TableTestSupport();
		parser = new EquationParserImpl();
	}

	Equation parseEquation(String equation, CyTable context) {
		Map<String, Class<?>> typeMap = new HashMap<>();
		for (CyColumn column : context.getColumns()) {
			Class<?> type = column.getType();
			if(type == Integer.class)
				type = Long.class;
			typeMap.put(column.getName(), type);
		}
		EquationCompilerImpl compiler = new EquationCompilerImpl(parser);
		assertTrue(compiler.compile(equation, typeMap));
		return compiler.getEquation();
	}
	
	
	@Test
	public void testVirtualColumnEquation() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table1 = factory.createTable("Table 1", "SUID", Long.class, true, true);
		CyTable table2 = factory.createTable("Table 2", "SUID", Long.class, true, true);
		CyTable table3 = factory.createTable("Table 3", "SUID", Long.class, true, true);
		table1.createColumn("name", String.class, false);
		table1.createColumn("real", String.class, false);
		table2.createColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		table3.createColumn("name", String.class, false);
		table3.addVirtualColumn("virtual2", "virtual", table2, "SUID", false);
		
		CyRow row1 = table1.getRow(1L);
		row1.set("name", "1");
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", "2");
		
		CyRow row3 = table3.getRow(1L);
		row3.set("name", "3");

		row3.set("virtual2", parseEquation("=$name", table3));
		
		// Equation should be propagated to all tables
		assertNotNull(row1.getRaw("real"));
		assertNotNull(row2.getRaw("virtual"));
		assertNotNull(row3.getRaw("virtual2"));
		
		// Equation should yield different values depending on context
		assertEquals("1", row1.get("real", String.class));
		assertEquals("2", row2.get("virtual", String.class));
		assertEquals("3", row3.get("virtual2", String.class));

		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		row1.set("real", "other");
		assertEquals("other", row1.get("real", String.class));
		assertEquals("other", row2.get("virtual", String.class));
		assertEquals("other", row3.get("virtual2", String.class));
	}
	
	@Test
	public void testVirtualColumnListEquation() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table1 = factory.createTable("Table 1", "SUID", Long.class, true, true);
		CyTable table2 = factory.createTable("Table 2", "SUID", Long.class, true, true);
		CyTable table3 = factory.createTable("Table 3", "SUID", Long.class, true, true);
		table1.createListColumn("name", String.class, false);
		table1.createListColumn("real", String.class, false);
		table2.createListColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		table3.createListColumn("name", String.class, false);
		table3.addVirtualColumn("virtual2", "virtual", table2, "SUID", false);
		
		List<String> list1 = new ArrayList<>();
		list1.add("1");
		
		List<String> list2 = new ArrayList<>();
		list2.add("2");
		
		List<String> list3 = new ArrayList<>();
		list3.add("3");

		CyRow row1 = table1.getRow(1L);
		row1.set("name", list1);
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", list2);
		
		CyRow row3 = table3.getRow(1L);
		row3.set("name", list3);

		row3.set("virtual2", parseEquation("=SLIST($name)", table3));
		
		// Equation should be propagated to all tables
		assertNotNull(row1.getRaw("real"));
		assertNotNull(row2.getRaw("virtual"));
		assertNotNull(row3.getRaw("virtual2"));
		
		// Equation should yield different values depending on context
		List<String> result1 = row1.getList("real", String.class);
		assertNotNull(result1);
		assertEquals(1, result1.size());
		assertEquals("1", result1.get(0));
		
		List<String> result2 = row2.getList("virtual", String.class);
		assertNotNull(result2);
		assertEquals(1, result2.size());
		assertEquals("2", result2.get(0));

		List<String> result3 = row3.getList("virtual2", String.class);
		assertNotNull(result3);
		assertEquals(1, result3.size());
		assertEquals("3", result3.get(0));

		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		List<String> other = new ArrayList<>();
		other.add("other");
		row1.set("real", other);
		
		List<String> resultOther1 = row1.getList("real", String.class);
		assertNotNull(resultOther1);
		assertEquals(1, resultOther1.size());
		assertEquals("other", resultOther1.get(0));

		List<String> resultOther2 = row2.getList("virtual", String.class);
		assertNotNull(resultOther2);
		assertEquals(1, resultOther2.size());
		assertEquals("other", resultOther2.get(0));

		List<String> resultOther3 = row3.getList("virtual2", String.class);
		assertNotNull(resultOther3);
		assertEquals(1, resultOther3.size());
		assertEquals("other", resultOther3.get(0));
	}
	
	
	@Test
	public void testEquationEvaluationThreadSafety() throws Exception {
		CyTableFactory factory = support.getTableFactory();
		CyTable table = factory.createTable("MyTable", "SUID", Long.class, true, true);
		// there was a bug using Integer at time of writing, so using Double
		table.createColumn("c1", Double.class, false);
		table.createColumn("e1", Double.class, false);
		table.createColumn("e2", Double.class, false);
		table.createColumn("e3", Double.class, false);
		
		Equation e1 = parseEquation("=$c1", table);
		Equation e2 = parseEquation("=$e1 + 1.0", table);
		Equation e3 = parseEquation("=$e2 + 1.0", table);
		
		// first test using only one row to make sure the equations work correctly
		{
			CyRow row = table.getRow(1L);
			row.set("c1", 1.0);
			row.set("e1", e1);
			row.set("e2", e2);
			row.set("e3", e3);
			
			Double result = row.get("e3", Double.class);
			assertEquals(3.0, result, 0);
		}
		
		// Ok now lets scale it up!
		
		final int N = 1000;
		for(int i = 1; i <= N; i++) {
			CyRow row = table.getRow((long)i);
			row.set("c1", 1.0);
			row.set("e1", e1);
			row.set("e2", e2);
			row.set("e3", e3);
		}
		
		assertEquals(N, table.getRowCount());
		
		List<Callable<Double>> taskList = new ArrayList<>(N);
		
		for(int i = 1; i <= N; i++) {
			final int n = i;
			taskList.add(new Callable<Double>() {
				public Double call() throws Exception {
					CyRow row = table.getRow((long)n);
					return row.get("e3", Double.class);
				}
			});
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(20);
		List<Future<Double>> futures = executor.invokeAll(taskList);
		
		double result = 0.0;
		for(Future<Double> f : futures) {
			result += f.get();
		}
		executor.shutdown();
		
		assertEquals(N * 3.0, result, 0);
	}
	
	
	@Test
	public void testVirtualColumnEquationDeleteFromChild() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table1 = factory.createTable("Table 1a", "SUID", Long.class, true, true);
		CyTable table2 = factory.createTable("Table 2b", "SUID", Long.class, true, true);
		CyTable table3 = factory.createTable("Table 3c", "SUID", Long.class, true, true);
		table1.createColumn("name", String.class, false);
		table1.createColumn("real", String.class, false);
		table2.createColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		table3.createColumn("name", String.class, false);
		table3.addVirtualColumn("virtual2", "virtual", table2, "SUID", false);
		
		CyRow row1 = table1.getRow(1L);
		row1.set("name", "1");
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", "2");
		
		CyRow row3 = table3.getRow(1L);
		row3.set("name", "3");

		row3.set("virtual2", parseEquation("=$name", table3));
		
		// Delete equation from child table should delete it from all tables
		row3.set("virtual2", null);
		assertNull(row1.get("real", String.class));
		assertNull(row2.get("virtual", String.class));
		assertNull(row3.get("virtual2", String.class));
	}
	
	@Test
	public void testVirtualColumnEquationDeleteFromParent() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table1 = factory.createTable("Table 1a", "SUID", Long.class, true, true);
		CyTable table2 = factory.createTable("Table 2b", "SUID", Long.class, true, true);
		CyTable table3 = factory.createTable("Table 3c", "SUID", Long.class, true, true);
		table1.createColumn("name", String.class, false);
		table1.createColumn("real", String.class, false);
		table2.createColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		table3.createColumn("name", String.class, false);
		table3.addVirtualColumn("virtual2", "virtual", table2, "SUID", false);
		
		CyRow row1 = table1.getRow(1L);
		row1.set("name", "1");
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", "2");
		
		CyRow row3 = table3.getRow(1L);
		row3.set("name", "3");

		row3.set("virtual2", parseEquation("=$name", table3));
		
		// Delete equation from parent table should delete it from all tables
		row1.set("real", null);
		assertNull(row1.get("real", String.class));
		assertNull(row2.get("virtual", String.class));
		assertNull(row3.get("virtual2", String.class));
	}
	
	@Test
	public void testChainEquationsUsingArithmeticOperators() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table = factory.createTable("MyTable2", "SUID", Long.class, true, true);
		table.createColumn("c1", Integer.class, false);
		table.createColumn("c2", Integer.class, false);
		table.createColumn("c3", Integer.class, false);
		
		Equation e2 = parseEquation("=$c1 + 1", table);
		Equation e3 = parseEquation("=$c2 + 1", table);
		
		CyRow row = table.getRow(1L);
		row.set("c1", 1);
		row.set("c2", e2);
		row.set("c3", e3);
		
		assertEquals(Integer.valueOf(1), row.get("c1", Integer.class));
		assertEquals(Integer.valueOf(2), row.get("c2", Integer.class));
		assertEquals(Integer.valueOf(3), row.get("c3", Integer.class));
	}
}

