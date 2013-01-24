package org.cytoscape.model;

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

import junit.framework.Assert;

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
		Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>();
		for (CyColumn column : context.getColumns()) {
			typeMap.put(column.getName(), column.getType());
		}
		EquationCompilerImpl compiler = new EquationCompilerImpl(parser);
		Assert.assertTrue(compiler.compile(equation, typeMap));
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
		Assert.assertNotNull(row1.getRaw("real"));
		Assert.assertNotNull(row2.getRaw("virtual"));
		Assert.assertNotNull(row3.getRaw("virtual2"));
		
		// Equation should yield different values depending on context
		Assert.assertEquals("1", row1.get("real", String.class));
		Assert.assertEquals("2", row2.get("virtual", String.class));
		Assert.assertEquals("3", row3.get("virtual2", String.class));

		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		row1.set("real", "other");
		Assert.assertEquals("other", row1.get("real", String.class));
		Assert.assertEquals("other", row2.get("virtual", String.class));
		Assert.assertEquals("other", row3.get("virtual2", String.class));
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
		
		List<String> list1 = new ArrayList<String>();
		list1.add("1");
		
		List<String> list2 = new ArrayList<String>();
		list2.add("2");
		
		List<String> list3 = new ArrayList<String>();
		list3.add("3");

		CyRow row1 = table1.getRow(1L);
		row1.set("name", list1);
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", list2);
		
		CyRow row3 = table3.getRow(1L);
		row3.set("name", list3);

		row3.set("virtual2", parseEquation("=SLIST($name)", table3));
		
		// Equation should be propagated to all tables
		Assert.assertNotNull(row1.getRaw("real"));
		Assert.assertNotNull(row2.getRaw("virtual"));
		Assert.assertNotNull(row3.getRaw("virtual2"));
		
		// Equation should yield different values depending on context
		List<String> result1 = row1.getList("real", String.class);
		Assert.assertNotNull(result1);
		Assert.assertEquals(1, result1.size());
		Assert.assertEquals("1", result1.get(0));
		
		List<String> result2 = row2.getList("virtual", String.class);
		Assert.assertNotNull(result2);
		Assert.assertEquals(1, result2.size());
		Assert.assertEquals("2", result2.get(0));

		List<String> result3 = row3.getList("virtual2", String.class);
		Assert.assertNotNull(result3);
		Assert.assertEquals(1, result3.size());
		Assert.assertEquals("3", result3.get(0));

		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		List<String> other = new ArrayList<String>();
		other.add("other");
		row1.set("real", other);
		
		List<String> resultOther1 = row1.getList("real", String.class);
		Assert.assertNotNull(resultOther1);
		Assert.assertEquals(1, resultOther1.size());
		Assert.assertEquals("other", resultOther1.get(0));

		List<String> resultOther2 = row2.getList("virtual", String.class);
		Assert.assertNotNull(resultOther2);
		Assert.assertEquals(1, resultOther2.size());
		Assert.assertEquals("other", resultOther2.get(0));

		List<String> resultOther3 = row3.getList("virtual2", String.class);
		Assert.assertNotNull(resultOther3);
		Assert.assertEquals(1, resultOther3.size());
		Assert.assertEquals("other", resultOther3.get(0));
}
}
