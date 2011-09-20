package org.cytoscape.io.internal.read.datatable;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;

import org.cytoscape.test.support.DataTableTestSupport;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableFactory;

import org.cytoscape.io.internal.read.AbstractNetworkViewReaderTester;

import static org.mockito.Mockito.*;

public class TextDataTableReaderTest {

	CyTableFactory tableFactory;
	TaskMonitor taskMonitor;

	public TextDataTableReaderTest() {
		taskMonitor = mock(TaskMonitor.class);
		tableFactory = new DataTableTestSupport().getDataTableFactory();
	}


	@Test
	public void testDefaultDelimiter() throws Exception {

		CyTable[] tables = getTables("table_tab.txt","\t");

		CyTable table = checkSingleTable(tables, 3, 4);
	} 

	@Test
	public void testCommaDelimiter() throws Exception {

		CyTable[] tables = getTables("table_comma.txt",",");

		CyTable table = checkSingleTable(tables, 3, 4);

	} 

	@Test
	public void testSpaceDelimiter() throws Exception {

		CyTable[] tables = getTables("table_space.txt"," ");

		CyTable table = checkSingleTable(tables, 3, 5);

	} 

	@Test
	public void testBlankDelimiter() throws Exception {

		CyTable[] tables = getTables("blank_delimiter.txt", "");

		// 12 is number of chars in the first line and no other rows 
		// have that number of chars, so no rows should be loaded.
		CyTable table = checkSingleTable(tables, 12, 0);
	}

	@Test(expected=IOException.class)
	public void testDuplicateHeaders() throws Exception {
		CyTable[] tables = getTables("table_space.txt", "");
	} 

	@Test
	public void testBadDelimiter() throws Exception {

		CyTable[] tables = getTables("table_tab.txt", ",");

		// Since there are no commas in the first line, we
		// should see only one column created and then each
		// subsequent row should work since they also don't
		// contain commas.
		CyTable table = checkSingleTable(tables, 1, 5);
	}

	private CyTable checkSingleTable( CyTable[] tables, int numCols, int numRows) {
		assertNotNull( tables );
		assertEquals( 1, tables.length );
		CyTable table = tables[0];
		assertNotNull( table );
		assertEquals( numRows, table.getAllRows().size() );
		assertEquals(numCols, table.getColumns().size());
		return table;
	}

	private CyTable[] getTables(String file, String delim) throws Exception {
		File f = new File("./src/test/resources/testData/datatable/" + file);
		TextDataTableReader snvp = new TextDataTableReader(new FileInputStream(f), tableFactory);
		snvp.delimiter = delim; // setting the tunable
		snvp.run(taskMonitor);

		return snvp.getCyTables();
	}
}
