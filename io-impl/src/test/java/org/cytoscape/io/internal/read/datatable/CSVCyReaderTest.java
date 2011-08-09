package org.cytoscape.io.internal.read.datatable;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CSVCyReaderTest {
	@Mock TaskMonitor taskMonitor;

	private CyTableFactory tableFactory;

	@Before
	public void setUp() {
		TableTestSupport tableTestSupport = new TableTestSupport();
		tableFactory = tableTestSupport.getTableFactory();
		MockitoAnnotations.initMocks(this);
	}

	InputStream createStream(String data) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(data.getBytes("UTF-8"));
	}

	@Test
	public void testReadSimple() throws Exception {
		String data = "SUID\r\njava.lang.Long\r\ntest table,\"public\\,mutable\"\r\n5\r\n6";
		CSVCyReader reader = new CSVCyReader(createStream(data), true,
						     /* handleEquations = */ false, tableFactory,
						     null);
		reader.run(taskMonitor);
		CyTable[] tables = reader.getCyTables();
		assertNotNull(tables);
		assertEquals(1, tables.length);
		CyTable table = tables[0];
		assertNotNull(table);
		assertEquals(2, table.getRowCount());
		CyRow row = table.getRow(5L);
		assertNotNull(row);
		Long value = row.get("SUID", Long.class);
		assertEquals((Long) 5L, value);
		assertTrue(table.isPublic());
		assertEquals(Mutability.MUTABLE, table.getMutability());
		assertEquals("test table", table.getTitle());
	}

	@Test
	public void testReadString() throws Exception {
		String data = "SUID,name\r\njava.lang.Long,java.lang.String\r\ntest table,\"public\\,mutable\"\r\n1,Alice\r\n2,Bob\r\n3,Carol";
		CSVCyReader reader = new CSVCyReader(createStream(data), true,
						     /* handleEquations = */ false, tableFactory,
						     null);
		reader.run(taskMonitor);
		CyTable[] tables = reader.getCyTables();
		CyTable table = tables[0];
		CyRow row = table.getRow(3L);
		assertEquals("Carol", row.get("name", String.class));
	}

	@Test
	public void testReadDouble() throws Exception {
		String data = "SUID,weight\r\njava.lang.Long,java.lang.Double\r\ntest table,\"public\\,mutable\"\r\n0,0.56\r\n-5,-1.234";
		CSVCyReader reader = new CSVCyReader(createStream(data), true,
						     /* handleEquations = */ false, tableFactory,
						     null);
		reader.run(taskMonitor);
		CyTable[] tables = reader.getCyTables();
		CyTable table = tables[0];
		CyRow row = table.getRow(-5L);
		assertEquals((Double) (-1.234), row.get("weight", Double.class));
	}

	@Test
	public void testReadBoolean() throws Exception {
		String data = "SUID,hidden\r\njava.lang.Long,java.lang.Boolean\r\ntest table,\"public\\,mutable\"\r\n30,true\r\n40,false\r\n50,true";
		CSVCyReader reader = new CSVCyReader(createStream(data), true,
						     /* handleEquations = */ false, tableFactory,
						     null);
		reader.run(taskMonitor);
		CyTable[] tables = reader.getCyTables();
		CyTable table = tables[0];
		CyRow row = table.getRow(50L);
		assertEquals(Boolean.TRUE, row.get("hidden", Boolean.class));
	}

	@Test
	public void testReadList() throws Exception {
		String data = "SUID,list\r\njava.lang.Long,java.util.List<java.lang.String>\r\ntest table,\"public\\,mutable\"\r\n1,\"a\rb\rc\"";
		CSVCyReader reader = new CSVCyReader(createStream(data), true,
						     /* handleEquations = */ false, tableFactory,
						     null);
		reader.run(taskMonitor);
		CyTable[] tables = reader.getCyTables();
		CyTable table = tables[0];
		CyRow row = table.getRow(1L);
		List<String> list = row.getList("list", String.class);
		assertEquals("c", list.get(2));
	}
}
