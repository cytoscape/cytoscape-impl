package org.cytoscape.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.internal.column.ColumnDataFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NamespaceTableTest {

	private CyTable table;
	
	@Before
	public void setUp() {
		CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		CyEventHelper eventHelper = new DummyCyEventHelper();
		EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));
		final Interpreter interpreter = new InterpreterImpl();
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		when(serviceRegistrar.getService(Interpreter.class)).thenReturn(interpreter);
		
		table = new CyTableImpl("homer", CyIdentifiable.SUID, Long.class, false, true, SavePolicy.SESSION_FILE,
				eventHelper, ColumnDataFactory.createDefaultFactory(), interpreter, 1000);
		CyTableManagerImpl tblMgr = new CyTableManagerImpl(new CyNetworkTableManagerImpl(), 
				new CyNetworkManagerImpl(serviceRegistrar), serviceRegistrar);
		tblMgr.addTable(table);

	}

	@After
	public void tearDown() {
		table = null;
	}
	
	
	@Test
	public void testColumnName() {
		table.createColumn("mynamespace::column1", Integer.class, false);
		CyColumn column1 = table.getColumn("mynamespace::column1");
		assertEquals("mynamespace::column1", column1.getName());
		assertEquals("column1", column1.getNameOnly());
		assertEquals("mynamespace", column1.getNamespace());
		
		table.createColumn("mynamespace","column2", Integer.class, false);
		CyColumn column2 = table.getColumn("mynamespace::column2");
		assertEquals("mynamespace::column2", column2.getName());
		assertEquals("column2", column2.getNameOnly());
		assertEquals("mynamespace", column2.getNamespace());
		
		table.createColumn("column3", Integer.class, false);
		CyColumn column3 = table.getColumn("column3");
		assertEquals("column3", column3.getName());
		assertEquals("column3", column3.getNameOnly());
		assertNull(column3.getNamespace());
		
		table.createColumn(null, "column4", Integer.class, false);
		CyColumn column4 = table.getColumn("column4");
		assertEquals("column4", column4.getName());
		assertEquals("column4", column4.getNameOnly());
		assertNull(column4.getNamespace());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateColumnSameName1() {
		table.createColumn("mynamespace::column1", Integer.class, false);
		table.createColumn("mynamespace","column1", Integer.class, false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateColumnSameName2() {
		table.createListColumn("mynamespace::column1", Integer.class, false);
		table.createListColumn("mynamespace","column1", Integer.class, false);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateColumnSameName3() {
		table.createColumn("mynamespace::column1", Integer.class, false);
		table.createListColumn("mynamespace","column1", Integer.class, false);
	}
	
	@Test
	public void testCreateColumn() {
		assertEquals(1, table.getColumns().size());
		
		table.createColumn("mynamespace::column1", Integer.class, false);
		table.createColumn("mynamespace","column2", Integer.class, false);
		assertEquals(3, table.getColumns().size());
		
		CyColumn column1a = table.getColumn("mynamespace::column1");
		CyColumn column1b = table.getColumn("mynamespace", "column1");
		assertSame(column1a, column1b);
		CyColumn column2a = table.getColumn("mynamespace::column2");
		CyColumn column2b = table.getColumn("mynamespace", "column2");
		assertSame(column2a, column2b);
		
		table.createListColumn("mynamespace::column3", Integer.class, false);
		table.createListColumn("mynamespace","column4", Integer.class, false);
		assertEquals(5, table.getColumns().size());
		
		CyColumn column3a = table.getColumn("mynamespace::column3");
		CyColumn column3b = table.getColumn("mynamespace", "column3");
		assertSame(column3a, column3b);
		CyColumn column4a = table.getColumn("mynamespace::column4");
		CyColumn column4b = table.getColumn("mynamespace", "column4");
		assertSame(column4a, column4b);
	}
	
	@Test
	public void testDeleteColumn() {
		assertEquals(1, table.getColumns().size());
		table.createColumn("mynamespace::column1", Integer.class, false);
		assertEquals(2, table.getColumns().size());
		table.deleteColumn("mynamespace::column1");
		assertEquals(1, table.getColumns().size());
		table.createColumn("mynamespace","column2", Integer.class, false);
		assertEquals(2, table.getColumns().size());
		table.deleteColumn("mynamespace","column2");
		assertEquals(1, table.getColumns().size());
	}
	
	@Test
	public void testRowGet() {
		table.createColumn("mynamespace::column1", Integer.class, false);
		
		table.getRow(1L).set("mynamespace::column1", 100);
		table.getRow(2L).set("mynamespace","column1", 200);
		
		assertEquals(2, table.getRowCount());
		
		assertTrue(table.getRow(1L).isSet("mynamespace","column1"));
		assertTrue(table.getRow(2L).isSet("mynamespace::column1"));
		
		assertEquals(100, table.getRow(1L).get("mynamespace","column1", Integer.class).intValue());
		assertEquals(200, table.getRow(2L).get("mynamespace::column1", Integer.class).intValue());
	}
	
	@Test
	public void testGetByNamespace() {
		table.createColumn("mynamespace1::column1", Integer.class, false);
		table.createColumn("mynamespace1::column2", Integer.class, false);
		
		table.createColumn("mynamespace2::column1", Integer.class, false);
		table.createColumn("mynamespace2::column2", Integer.class, false);
		table.createColumn("mynamespace2::column3", Integer.class, false);
		
		Collection<String> namespaces = table.getNamespaces();
		assertEquals(3, namespaces.size());
		assertTrue(namespaces.contains(null));
		assertTrue(namespaces.contains("mynamespace1"));
		assertTrue(namespaces.contains("mynamespace2"));
		
		Collection<CyColumn> columns1 = table.getColumns("mynamespace1");
		assertEquals(2, columns1.size());
		List<String> names1 = columns1.stream().map(CyColumn::getName).sorted().collect(Collectors.toList());
		assertEquals("mynamespace1::column1", names1.get(0));
		assertEquals("mynamespace1::column2", names1.get(1));
		
		Collection<CyColumn> columns2 = table.getColumns("mynamespace2");
		assertEquals(3, columns2.size());
		List<String> names2 = columns2.stream().map(CyColumn::getName).sorted().collect(Collectors.toList());
		assertEquals("mynamespace2::column1", names2.get(0));
		assertEquals("mynamespace2::column2", names2.get(1));
		assertEquals("mynamespace2::column3", names2.get(2));
		
		Collection<CyColumn> columns3 = table.getColumns(null);
		assertEquals(1, columns3.size());
		assertEquals(CyIdentifiable.SUID, columns3.iterator().next().getName());
	}
	
	@Test
	public void testSearch() {
		table.createColumn("mynamespace::column1", String.class, false);
		
		table.getRow(1L).set("mynamespace::column1", "odd");
		table.getRow(2L).set("mynamespace::column1", "even");
		table.getRow(3L).set("mynamespace::column1", "odd");
		table.getRow(4L).set("mynamespace::column1", "even");
		table.getRow(5L).set("mynamespace::column1", "odd");
		
		Collection<CyRow> oddRows = table.getMatchingRows("mynamespace::column1", "odd");
		assertEquals(3, oddRows.size());
		oddRows = table.getMatchingRows("mynamespace","column1", "odd");
		assertEquals(3, oddRows.size());
		
		Collection<String> evenRows = table.getMatchingKeys("mynamespace::column1", "even", String.class);
		assertEquals(2, evenRows.size());
		evenRows = table.getMatchingKeys("mynamespace","column1", "even", String.class);
		assertEquals(2, evenRows.size());
		
		assertEquals(3, table.countMatchingRows("mynamespace::column1", "odd"));
		assertEquals(2, table.countMatchingRows("mynamespace::column1", "even"));
	}
	
}
