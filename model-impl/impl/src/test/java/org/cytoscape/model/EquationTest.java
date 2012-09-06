package org.cytoscape.model;

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
		table1.createColumn("name", String.class, false);
		table1.createColumn("real", String.class, false);
		table2.createColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		
		CyRow row1 = table1.getRow(1L);
		row1.set("name", "1");
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", "2");
		
		row2.set("virtual", parseEquation("=$name", table2));
		
		// Equation should be propagated to both tables
		Assert.assertNotNull(row1.getRaw("real"));
		Assert.assertNotNull(row2.getRaw("virtual"));
		
		// Equation should yield different values depending on context
		Assert.assertEquals("1", row1.get("real", String.class));
		Assert.assertEquals("2", row2.get("virtual", String.class));

		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		row1.set("real", "3");
		Assert.assertEquals("3", row1.get("real", String.class));
		Assert.assertEquals("3", row2.get("virtual", String.class));
	}
	
	@Test
	public void testVirtualColumnListEquation() {
		CyTableFactory factory = support.getTableFactory();
		CyTable table1 = factory.createTable("Table 1", "SUID", Long.class, true, true);
		CyTable table2 = factory.createTable("Table 2", "SUID", Long.class, true, true);
		table1.createListColumn("name", String.class, false);
		table1.createListColumn("real", String.class, false);
		table2.createListColumn("name", String.class, false);
		table2.addVirtualColumn("virtual", "real", table1, "SUID", false);
		
		List<String> list1 = new ArrayList<String>();
		list1.add("1");
		
		List<String> list2 = new ArrayList<String>();
		list2.add("2");
		
		CyRow row1 = table1.getRow(1L);
		row1.set("name", list1);
		
		CyRow row2 = table2.getRow(1L);
		row2.set("name", list2);
		
		row2.set("virtual", parseEquation("=SLIST($name)", table2));
		
		// Equation should be propagated to both tables
		Assert.assertNotNull(row1.getRaw("real"));
		Assert.assertNotNull(row2.getRaw("virtual"));
		
		// Equation should yield different values depending on context
		List<String> result1 = row1.getList("real", String.class);
		Assert.assertNotNull(result1);
		Assert.assertEquals(1, result1.size());
		Assert.assertEquals("1", result1.get(0));
		
		List<String> result2 = row2.getList("virtual", String.class);
		Assert.assertNotNull(result2);
		Assert.assertEquals(1, result2.size());
		Assert.assertEquals("2", result2.get(0));
		
		// Overwrite equation with normal attribute.  Both ends of
		// VirtualColumn should give the same answer.
		List<String> list3 = new ArrayList<String>();
		list3.add("3");
		row1.set("real", list3);
		
		List<String> result3a = row1.getList("real", String.class);
		Assert.assertNotNull(result3a);
		Assert.assertEquals(1, result3a.size());
		Assert.assertEquals("3", result3a.get(0));

		List<String> result3b = row2.getList("virtual", String.class);
		Assert.assertNotNull(result3b);
		Assert.assertEquals(1, result3b.size());
		Assert.assertEquals("3", result3b.get(0));
	}
}
